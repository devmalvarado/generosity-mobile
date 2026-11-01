import { createHash, createHmac, randomBytes, timingSafeEqual } from 'node:crypto';
import bcrypt from 'bcryptjs';
import { HttpError, Problem, forbidden, notFound, unauthenticated } from './problems.js';
import { iso, publicUser } from './store.js';

const DUMMY_HASH = '$2y$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.';
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const LOCALE = /^[a-z]{2,3}(-[A-Za-z0-9]{2,8})*$/;

function phpJson(value) {
  return JSON.stringify(value).replace(/[\u007f-￿]/g, (char) => `\\u${char.charCodeAt(0).toString(16).padStart(4, '0')}`).replace(/\//g, '\\/');
}

function canonical(value) {
  if (Array.isArray(value)) {
    return value.map(canonical);
  }
  if (value && typeof value === 'object') {
    return Object.fromEntries(Object.keys(value).sort().map((key) => [key, canonical(value[key])]));
  }
  return value;
}

function same(left, right) {
  const a = Buffer.from(left);
  const b = Buffer.from(right);
  return a.length === b.length && timingSafeEqual(a, b);
}

function hashPassword(password) {
  return bcrypt.hash(password, 12).then((hash) => hash.replace(/^\$2a\$/, '$2y$'));
}

function checkPassword(password, hash) {
  return bcrypt.compare(password, String(hash).replace(/^\$2y\$/, '$2a$'));
}

function fail(errors) {
  throw new Problem(422, 'validation_failed', errors);
}

function requiredString(body, key, max, errors) {
  const value = body[key];
  if (typeof value !== 'string' || value.length === 0 || value.length > max) {
    errors[key] = ['Invalid value.'];
    return undefined;
  }
  return value;
}

function optionalString(body, key, max, errors) {
  if (body[key] === undefined || body[key] === null) {
    return null;
  }
  return requiredString(body, key, max, errors);
}

function prohibit(body, keys, errors) {
  for (const key of keys) {
    if (Object.hasOwn(body, key)) {
      errors[key] = ['This field is not allowed.'];
    }
  }
}

function accepted(value) {
  return value === true || value === 1 || value === '1' || value === 'yes' || value === 'on';
}

function integer(value, min, max) {
  return Number.isInteger(value) && value >= min && value <= max;
}

function localized(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return false;
  }
  const entries = Object.entries(value);
  if (entries.length < 1 || entries.length > 20) {
    return false;
  }
  return entries.every(([locale, text]) => LOCALE.test(locale) && typeof text === 'string' && text.trim() !== '' && text.length <= 4000);
}

function pageNumber(url) {
  const raw = url.searchParams.get('page');
  if (raw === null) {
    return 1;
  }
  if (!/^[1-9]\d*$/.test(raw)) {
    fail({ page: ['Invalid value.'] });
  }
  const page = Number(raw);
  if (page > 100000) {
    fail({ page: ['Invalid value.'] });
  }
  return page;
}

function paginate(req, rows, page, perPage) {
  const last = Math.max(1, Math.ceil(rows.total / perPage));
  const link = (target) => {
    if (target === null) {
      return null;
    }
    const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    url.searchParams.set('page', String(target));
    return url.toString();
  };
  return {
    data: rows.rows,
    current_page: page,
    per_page: perPage,
    total: rows.total,
    last_page: last,
    next_page_url: page < last ? link(page + 1) : null,
    prev_page_url: page > 1 ? link(page - 1) : null,
  };
}

function publicCenter(center) {
  return { ...center.details, id: center.id, version: center.version, updatedAt: center.updated_at };
}

function publicNeed(need) {
  return {
    ...need.details,
    id: need.id,
    centerId: need.center_id,
    status: need.status,
    version: need.version,
    quantityRequired: need.quantity_required,
    quantityCommitted: need.quantity_committed,
    expiresOnIsoDate: need.expires_on,
    updatedAt: need.updated_at,
  };
}

function syncNeed(need, today) {
  const row = publicNeed(need);
  const selected = {
    id: row.id,
    centerId: row.centerId,
    title: row.title,
    description: row.description,
    helpMethods: row.helpMethods,
    urgent: row.urgent,
    quantityRequired: row.quantityRequired,
    expiresOnIsoDate: row.expiresOnIsoDate,
    unit: row.unit,
  };
  if (row.quantityRequired !== null) {
    selected.quantityCommitted = row.quantityCommitted;
  }
  selected.status = row.expiresOnIsoDate && row.expiresOnIsoDate < today ? 'expired'
    : (row.quantityRequired !== null && row.quantityCommitted >= row.quantityRequired ? 'covered' : 'active');
  return selected;
}

function applicationSummary(row) {
  return {
    id: row.id,
    organization_name: row.details.organization_name,
    site_name: row.details.site_name,
    status: row.status,
    submitted_at: row.created_at,
  };
}

function todayUtc(now) {
  return now.toISOString().slice(0, 10);
}

function operationHash(method, path, data) {
  return createHash('sha256').update(`${method} ${path} ${phpJson(canonical(data))}`).digest('hex');
}

async function mutate(req, store, data, authorize, write) {
  const key = req.headers['idempotency-key'];
  if (typeof key !== 'string' || !UUID.test(key)) {
    fail({ key: ['Invalid value.'] });
  }
  const hash = operationHash(req.method, req.path, data);
  return store.transaction(async (tx) => {
    const user = await tx.findUserById(req.user.id);
    const token = await tx.findToken(req.tokenId, req.tokenHash, req.now);
    if (!user || !token) {
      throw unauthenticated();
    }
    await authorize(tx);
    const prior = await tx.getOperation(user.id, key);
    if (prior) {
      if (!same(prior.request_hash, hash)) {
        throw new Problem(409, 'operation_reused');
      }
      return { status: prior.status, body: prior.response };
    }
    const response = await write(tx);
    await tx.insertOperation({
      user_id: user.id,
      key,
      request_hash: hash,
      response: response.body,
      status: response.status,
      created_at: iso(req.now),
    });
    return response;
  });
}

async function requireStaff(tx, userId, centerId) {
  if (!await tx.getCenter(centerId)) {
    throw notFound();
  }
  if (!await tx.isStaff(userId, centerId)) {
    throw forbidden();
  }
}

function passwordPolicy(password, creating, errors, key = 'password') {
  if (typeof password !== 'string' || Buffer.byteLength(password) > 72) {
    errors[key] = ['The password is too long. Use fewer characters.'];
    return;
  }
  if (!creating) {
    return;
  }
  if (password.length < 12 || !/[a-z]/.test(password) || !/[A-Z]/.test(password) || !/[0-9]/.test(password)) {
    errors[key] = ['Use at least 12 characters with upper case, lower case and a number.'];
  }
}

function registerBody(body, policyVersion) {
  const errors = {};
  const name = requiredString(body, 'name', 120, errors);
  const email = requiredString(body, 'email', 254, errors);
  passwordPolicy(body.password, true, errors);
  if (!accepted(body.adult_confirmed)) errors.adult_confirmed = ['Required.'];
  if (!accepted(body.terms_accepted)) errors.terms_accepted = ['Required.'];
  if (!accepted(body.privacy_acknowledged)) errors.privacy_acknowledged = ['Required.'];
  if (body.policy_version !== policyVersion) errors.policy_version = ['Invalid value.'];
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = ['Invalid value.'];
  if (Object.keys(errors).length) fail(errors);
  return { name, email: email.toLowerCase(), password: body.password };
}

function loginBody(body) {
  const errors = {};
  const email = requiredString(body, 'email', 254, errors);
  passwordPolicy(body.password, false, errors);
  const device = requiredString(body, 'device_name', 100, errors);
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = ['Invalid value.'];
  if (Object.keys(errors).length) fail(errors);
  return { email: email.toLowerCase(), password: body.password, device_name: device };
}

function needBody(body, updating) {
  const errors = {};
  if (!localized(body.title)) errors.title = ['Use language codes and non-empty text of at most 4000 characters.'];
  if (!localized(body.description)) errors.description = ['Use language codes and non-empty text of at most 4000 characters.'];
  if (!Array.isArray(body.helpMethods) || body.helpMethods.length < 1 || body.helpMethods.length > 3
    || new Set(body.helpMethods).size !== body.helpMethods.length
    || body.helpMethods.some((item) => !['goods', 'time', 'money'].includes(item))) {
    errors.helpMethods = ['Invalid value.'];
  }
  if (typeof body.urgent !== 'boolean') errors.urgent = ['Invalid value.'];
  if (!['draft', 'published', 'closed'].includes(body.status)) errors.status = ['Invalid value.'];
  if (body.expiresOnIsoDate !== undefined && body.expiresOnIsoDate !== null && !/^\d{4}-\d{2}-\d{2}$/.test(body.expiresOnIsoDate)) {
    errors.expiresOnIsoDate = ['Invalid value.'];
  }
  if (body.quantityRequired !== undefined && body.quantityRequired !== null && !integer(body.quantityRequired, 1, 1000000)) {
    errors.quantityRequired = ['Invalid value.'];
  }
  if (body.unit !== undefined && body.unit !== null && (typeof body.unit !== 'string' || body.unit.length > 60)) {
    errors.unit = ['Invalid value.'];
  }
  if (updating && !integer(body.version, 1, Number.MAX_SAFE_INTEGER)) errors.version = ['Invalid value.'];
  if (Object.keys(errors).length) fail(errors);
  return {
    title: body.title,
    description: body.description,
    helpMethods: body.helpMethods,
    urgent: body.urgent,
    unit: body.unit ?? null,
    quantityRequired: body.quantityRequired ?? null,
    expiresOnIsoDate: body.expiresOnIsoDate ?? null,
    status: body.status,
    version: body.version,
  };
}

function contributionBody(body) {
  const errors = {};
  prohibit(body, ['center_id', 'user_id', 'status', 'provenance'], errors);
  if (!['goods', 'time'].includes(body.method)) errors.method = ['Invalid value.'];
  if (body.need_id !== undefined && body.need_id !== null && (typeof body.need_id !== 'string' || body.need_id.length > 100)) {
    errors.need_id = ['Invalid value.'];
  }
  if (body.method === 'goods') {
    if (!integer(body.quantity, 1, 1000000)) errors.quantity = ['Invalid value.'];
    if (Object.hasOwn(body, 'minutes')) errors.minutes = ['This field is not allowed.'];
  }
  if (body.method === 'time') {
    if (!integer(body.minutes, 1, 525600)) errors.minutes = ['Invalid value.'];
    if (typeof body.activity !== 'string' || body.activity.length === 0 || body.activity.length > 500) errors.activity = ['Invalid value.'];
    if (Object.hasOwn(body, 'quantity')) errors.quantity = ['This field is not allowed.'];
  }
  for (const [key, max] of [['availability', 500], ['notes', 2000], ['quantity_unit', 60]]) {
    if (body[key] !== undefined && body[key] !== null && (typeof body[key] !== 'string' || body[key].length > max)) errors[key] = ['Invalid value.'];
  }
  if (Object.keys(errors).length) fail(errors);
  return {
    need_id: body.need_id ?? null,
    method: body.method,
    quantity: body.method === 'goods' ? body.quantity : undefined,
    minutes: body.method === 'time' ? body.minutes : undefined,
    activity: body.activity ?? null,
    availability: body.availability ?? null,
    notes: body.notes ?? null,
    quantity_unit: body.quantity_unit ?? null,
  };
}

function applicationBody(body) {
  const errors = {};
  prohibit(body, ['user_id', 'status', 'verified', 'center_id', 'contact_email'], errors);
  const data = {
    organization_name: requiredString(body, 'organization_name', 200, errors),
    legal_name: optionalString(body, 'legal_name', 200, errors),
    site_name: requiredString(body, 'site_name', 200, errors),
    activities: requiredString(body, 'activities', 2000, errors),
    country: requiredString(body, 'country', 100, errors),
    region: optionalString(body, 'region', 100, errors),
    city: requiredString(body, 'city', 100, errors),
    postal_code: optionalString(body, 'postal_code', 20, errors),
    address: optionalString(body, 'address', 500, errors),
    contact_name: requiredString(body, 'contact_name', 160, errors),
    contact_role: requiredString(body, 'contact_role', 160, errors),
    phone: optionalString(body, 'phone', 40, errors),
    website: optionalString(body, 'website', 2048, errors),
  };
  if (data.phone && !/^\+?[0-9 ()\-.]{6,40}$/.test(data.phone)) errors.phone = ['Invalid value.'];
  if (data.website && !/^https?:\/\/\S+$/i.test(data.website)) errors.website = ['Invalid value.'];
  if (!accepted(body.representation_confirmed)) errors.representation_confirmed = ['Required.'];
  if (!accepted(body.contact_consent)) errors.contact_consent = ['Required.'];
  if (Object.keys(errors).length) fail(errors);
  data.representation_confirmed = true;
  data.contact_consent = true;
  return canonical(data);
}

function visitBody(body, now) {
  const errors = {};
  prohibit(body, ['user_id', 'verification_status'], errors);
  const centerId = requiredString(body, 'center_id', 100, errors);
  if (typeof body.occurred_at !== 'string' || body.occurred_at.length > 64 || Number.isNaN(Date.parse(body.occurred_at))) {
    errors.occurred_at = ['Invalid value.'];
  } else if (Date.parse(body.occurred_at) > now.getTime() + 5 * 60 * 1000) {
    errors.occurred_at = ['Invalid value.'];
  }
  if (!accepted(body.explicit_action)) errors.explicit_action = ['Required.'];
  if (Object.keys(errors).length) fail(errors);
  return { center_id: centerId, occurred_at: new Date(body.occurred_at).toISOString(), explicit_action: true };
}

async function issueToken(tx, user, device, now, days) {
  const expires = new Date(now.getTime() + days * 24 * 60 * 60 * 1000);
  const secret = randomBytes(20).toString('hex');
  const hash = createHash('sha256').update(secret).digest('hex');
  const row = await tx.insertToken({ userId: user.id, name: device, hash, expiresAt: expires });
  return {
    token: `${row.id}|${secret}`,
    token_type: 'Bearer',
    expires_at: iso(expires),
    user: publicUser(user),
  };
}

function signedVerification(config, user) {
  const hash = createHash('sha1').update(user.email).digest('hex');
  const expires = Math.floor(config.now().getTime() / 1000) + config.verificationSeconds;
  const unsigned = `${config.appUrl}/api/v1/auth/verify/${user.id}/${hash}?expires=${expires}`;
  const signature = createHmac('sha256', config.appKey).update(unsigned).digest('hex');
  return `${unsigned}&signature=${signature}`;
}

function rateLimit(buckets, key, limit, now) {
  const window = 60 * 1000;
  const bucket = buckets.get(key) || [];
  const fresh = bucket.filter((at) => now - at < window);
  if (fresh.length >= limit) {
    const retry = Math.max(1, Math.ceil((fresh[0] + window - now) / 1000));
    const error = new HttpError(429, 'rate_limited');
    error.retryAfter = retry;
    throw error;
  }
  fresh.push(now);
  buckets.set(key, fresh);
}

function readBearer(header) {
  const match = /^Bearer ([A-Za-z0-9|]+)$/.exec(header || '');
  if (!match || !match[1].includes('|')) {
    return null;
  }
  const [id, secret] = match[1].split('|', 2);
  if (!/^[1-9]\d*$/.test(id) || !secret) {
    return null;
  }
  return { id: Number(id), hash: createHash('sha256').update(secret).digest('hex') };
}

const routes = [
  ['GET', '/health', false, health],
  ['GET', '/centers', false, centers],
  ['GET', '/centers/{center}', false, center],
  ['GET', '/needs', false, needs],
  ['GET', '/needs/{need}', false, need],
  ['POST', '/auth/register', false, register],
  ['POST', '/auth/login', false, login],
  ['POST', '/auth/verification', false, resend],
  ['POST', '/auth/forgot-password', false, forgot],
  ['POST', '/auth/reset-password', false, reset],
  ['GET', '/auth/verify/{id}/{hash}', false, verify],
  ['POST', '/auth/exchange', false, exchange],
  ['GET', '/sync/catalog', false, catalog],
  ['GET', '/me', true, me],
  ['POST', '/auth/logout', true, logout],
  ['GET', '/me/contributions', true, myContributions],
  ['GET', '/me/impact', true, impact],
  ['POST', '/contributions', true, createContribution],
  ['GET', '/contributions/{contribution}', true, showContribution],
  ['POST', '/contributions/{contribution}/transition', true, transition],
  ['GET', '/staff/centers', true, myCenters],
  ['GET', '/staff/centers/{center}/contributions', true, centerContributions],
  ['GET', '/staff/centers/{center}/needs', true, staffNeeds],
  ['POST', '/staff/centers/{center}/needs', true, createNeed],
  ['PUT', '/staff/centers/{center}/needs/{need}', true, updateNeed],
  ['GET', '/sync/account', true, account],
  ['POST', '/visits', true, visit],
  ['POST', '/center-applications', true, apply],
  ['GET', '/me/center-applications', true, myApplications],
];

export function contractOperations(spec) {
  return Object.entries(spec.paths).flatMap(([path, methods]) => Object.keys(methods)
    .filter((method) => ['get', 'post', 'put', 'patch', 'delete'].includes(method))
    .map((method) => `${method.toUpperCase()} ${path}`));
}

export function implementedOperations() {
  return routes.map(([method, path]) => `${method} ${path}`);
}

export function createApp({ config, store, mail = [] }) {
  const buckets = new Map();
  async function handle(req, res) {
    const now = config.now();
    const url = new URL(req.url, config.appUrl);
    const origin = req.headers.origin;
    const finish = (status, body, extra = {}) => {
      const headers = {
        'cache-control': 'no-store',
        'x-content-type-options': 'nosniff',
        ...extra,
      };
      if (origin && config.allowedOrigins.includes(origin)) {
        headers['access-control-allow-origin'] = origin;
        headers['access-control-allow-credentials'] = 'true';
        headers.vary = 'Origin';
      }
      if (body === undefined) {
        res.writeHead(status, headers);
        res.end();
        return;
      }
      headers['content-type'] = 'application/json; charset=utf-8';
      res.writeHead(status, headers);
      res.end(JSON.stringify(body));
    };
    try {
      if (req.method === 'OPTIONS') {
        finish(204, undefined, {
          'access-control-allow-methods': 'GET, POST, PUT, OPTIONS',
          'access-control-allow-headers': 'Authorization, Content-Type, Idempotency-Key, X-XSRF-TOKEN',
        });
        return;
      }
      if (!url.pathname.startsWith('/api/v1/') && url.pathname !== '/api/v1') {
        throw notFound();
      }
      const path = url.pathname.slice('/api/v1'.length) || '/';
      rateLimit(buckets, `api:${req.socket.remoteAddress}`, config.limits?.api || 120, now.getTime());
      const match = matchRoute(path, req.method);
      if (!match) {
        throw notFound();
      }
      const raw = await readBody(req);
      const json = raw.length ? JSON.parse(raw) : {};
      if (raw.length && (json === null || typeof json !== 'object' || Array.isArray(json))) {
        fail({ body: ['Invalid value.'] });
      }
      const context = {
        method: req.method,
        path: `api/v1${match.path}`,
        url,
        headers: req.headers,
        params: match.params,
        body: json,
        now,
        config,
        store,
        mail,
        ip: req.socket.remoteAddress || '',
      };
      if (match.auth) {
        const bearer = readBearer(req.headers.authorization);
        const token = bearer && await store.findToken(bearer.id, bearer.hash, now);
        const user = token && await store.findUserById(token.user_id);
        if (!bearer || !token || !user) {
          throw unauthenticated();
        }
        if (!user.email_verified_at) {
          throw forbidden();
        }
        context.user = user;
        context.tokenId = token.id;
        context.tokenHash = token.token;
      }
      const result = await match.handler(context);
      finish(result.status, result.body, result.headers);
    } catch (error) {
      if (error instanceof SyntaxError) {
        finish(422, { error: { code: 'validation_failed', details: { body: ['Invalid value.'] } } });
        return;
      }
      if (error instanceof Problem || error instanceof HttpError) {
        const headers = error.retryAfter ? { 'retry-after': String(error.retryAfter) } : {};
        finish(error.status, { error: { code: error.code, details: error.details } }, headers);
        return;
      }
      if (error?.status === 413) {
        finish(413, { error: { code: 'service_unavailable', details: {} } });
        return;
      }
      console.error('backend_failure', error?.name || 'Error');
      finish(500, { error: { code: 'service_unavailable', details: {} } });
    }
  }
  return { handle, store, mail, config, routes: implementedOperations() };
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    let size = 0;
    req.on('data', (chunk) => {
      size += chunk.length;
      if (size > 1_000_000) {
        const error = new Error('payload');
        error.status = 413;
        reject(error);
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')));
    req.on('error', reject);
  });
}

async function health({ store }) {
  if (!await store.health()) {
    throw new Problem(503, 'service_unavailable', {});
  }
  return { status: 200, body: { status: 'ready', apiVersion: 1, payments: 'disabled' } };
}

async function centers({ url, store }) {
  const page = pageNumber(url);
  const all = (await store.listCenters()).map(publicCenter);
  return { status: 200, body: paginate({ url, headers: { host: url.host } }, await store.page(all, page, 25), page, 25) };
}

async function center({ params, store }) {
  const found = await store.getCenter(params.center);
  if (!found) throw notFound();
  return { status: 200, body: publicCenter(found) };
}

async function needs({ url, store }) {
  const page = pageNumber(url);
  const centerId = url.searchParams.get('center_id');
  if (centerId && centerId.length > 100) fail({ center_id: ['Invalid value.'] });
  const all = (await store.listNeeds()).filter((item) => item.status === 'published' && (!centerId || item.center_id === centerId)).map(publicNeed);
  return { status: 200, body: paginate({ url, headers: { host: url.host } }, await store.page(all, page, 25), page, 25) };
}

async function need({ params, store }) {
  const found = await store.getNeed(params.need);
  if (!found || found.status !== 'published') throw notFound();
  return { status: 200, body: publicNeed(found) };
}

async function register({ body, config, store, mail, ip, now }) {
  limitAccount(config, ip, body?.email, now);
  const data = registerBody(body, config.policyVersion);
  await store.transaction(async (tx) => {
    const existing = await tx.findUserByEmailAny(data.email);
    if (existing) return;
    const user = await tx.insertUser({
      ...data,
      password: await hashPassword(data.password),
      policy_version: config.policyVersion,
      policy_accepted_at: iso(now),
      adult_confirmed_at: iso(now),
      created_at: now,
    });
    mail.push({ to: user.email, kind: 'verify', url: signedVerification(config, user) });
  });
  return { status: 202, body: { status: 'verification_requested' } };
}

async function login({ body, config, store, ip, now }) {
  limitAccount(config, ip, body?.email, now);
  const data = loginBody(body);
  return store.transaction(async (tx) => {
    const user = await tx.findActiveUserByEmail(data.email);
    const hash = user?.password || DUMMY_HASH;
    if (Buffer.byteLength(data.password) > 72 || !await checkPassword(data.password, hash) || !user) {
      throw new Problem(401, 'invalid_credentials');
    }
    if (!user.email_verified_at) throw new Problem(403, 'email_not_verified');
    return { status: 200, body: await issueToken(tx, user, data.device_name, now, config.tokenDays) };
  });
}

async function resend({ body, config, store, mail, ip, now }) {
  limitAccount(config, ip, body?.email, now);
  const errors = {};
  const email = requiredString(body, 'email', 254, errors);
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = ['Invalid value.'];
  if (Object.keys(errors).length) fail(errors);
  const user = await store.findActiveUserByEmail(email.toLowerCase());
  if (user && !user.email_verified_at) mail.push({ to: user.email, kind: 'verify', url: signedVerification(config, user) });
  return { status: 202, body: { status: 'verification_requested' } };
}

async function forgot({ body, config, store, mail, ip, now }) {
  limitAccount(config, ip, body?.email, now);
  const errors = {};
  const email = requiredString(body, 'email', 254, errors);
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = ['Invalid value.'];
  if (Object.keys(errors).length) fail(errors);
  const user = await store.findActiveUserByEmail(email.toLowerCase());
  if (user) {
    const token = randomBytes(32).toString('hex');
    await store.saveReset({ email: user.email, token: await hashPassword(token), created_at: now.getTime() });
    mail.push({ to: user.email, kind: 'reset', token });
  }
  return { status: 202, body: { status: 'reset_requested' } };
}

async function reset({ body, config, store, ip, now }) {
  limitAccount(config, ip, body?.email, now);
  const errors = {};
  const email = requiredString(body, 'email', 254, errors);
  const token = requiredString(body, 'token', 255, errors);
  passwordPolicy(body.password, true, errors);
  if (body.password !== body.password_confirmation) errors.password_confirmation = ['Invalid value.'];
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = ['Invalid value.'];
  if (Object.keys(errors).length) fail(errors);
  const normalized = email.toLowerCase();
  await store.transaction(async (tx) => {
    const user = await tx.findActiveUserByEmail(normalized);
    const saved = await tx.getReset(normalized);
    const fresh = saved && now.getTime() - saved.created_at <= config.resetSeconds * 1000;
    if (!user || !fresh || !await checkPassword(token, saved.token)) throw new Problem(422, 'invalid_reset_token');
    await tx.updateUser(user.id, { password: await hashPassword(body.password), remember_token: randomBytes(30).toString('hex'), updated_at: now });
    await tx.deleteUserTokens(user.id);
    await tx.deleteUserHandoffs(user.id);
    await tx.deleteReset(normalized);
  });
  return { status: 200, body: { status: 'password_reset' } };
}

async function verify({ params, url, config, store }) {
  const expires = url.searchParams.get('expires');
  const signature = url.searchParams.get('signature');
  if (!expires || !signature) fail({ signature: ['Invalid value.'] });
  const unsigned = `${config.appUrl}/api/v1/auth/verify/${params.id}/${params.hash}?expires=${expires}`;
  const expected = createHmac('sha256', config.appKey).update(unsigned).digest('hex');
  if (!same(expected, signature) || Number(expires) < Math.floor(config.now().getTime() / 1000)) throw forbidden();
  const user = await store.findUserById(Number(params.id));
  if (!user || !same(createHash('sha1').update(user.email).digest('hex'), params.hash)) throw user ? forbidden() : notFound();
  if (!user.email_verified_at) await store.updateUser(user.id, { email_verified_at: iso(config.now()), updated_at: config.now() });
  return { status: 200, body: { status: 'email_verified' } };
}

async function exchange({ body, config, store, ip, now }) {
  rememberIdentity(config, ip, now);
  const errors = {};
  if (typeof body.code !== 'string' || !/^[A-Za-z0-9]{64}$/.test(body.code)) errors.code = ['Invalid value.'];
  if (typeof body.code_verifier !== 'string' || !/^[A-Za-z0-9._~-]{43,128}$/.test(body.code_verifier)) errors.code_verifier = ['Invalid value.'];
  if (!['android', 'ios'].includes(body.client)) errors.client = ['Invalid value.'];
  if (Object.keys(errors).length) fail(errors);
  return store.transaction(async (tx) => {
    const id = createHash('sha256').update(body.code).digest('hex');
    const handoff = await tx.getHandoff(id);
    const challenge = createHash('sha256').update(body.code_verifier).digest('base64url');
    if (!handoff || new Date(handoff.expires_at).getTime() <= now.getTime() || handoff.client !== body.client || !same(handoff.challenge, challenge)) {
      throw new Problem(401, 'invalid_handoff');
    }
    await tx.deleteHandoff(id);
    const user = await tx.findUserById(handoff.user_id);
    if (!user) throw new Problem(401, 'invalid_handoff');
    return { status: 200, body: await issueToken(tx, user, body.client, now, config.tokenDays) };
  });
}

function rememberIdentity(config, ip, now) {
  if (!config._identityBuckets) config._identityBuckets = new Map();
  rateLimit(config._identityBuckets, `identity:${ip}`, config.limits?.identity || 20, now.getTime());
}

function limitAccount(config, ip, email, now) {
  if (!config._accountBuckets) config._accountBuckets = new Map();
  const account = typeof email === 'string' ? email.toLowerCase() : '';
  rateLimit(config._accountBuckets, `ip:${ip}`, config.limits?.accountIp || 10, now.getTime());
  rateLimit(config._accountBuckets, `account:${createHash('sha256').update(account).digest('hex')}`, config.limits?.account || 5, now.getTime());
}

async function catalog({ store, now }) {
  const listedCenters = await store.listCenters();
  if (listedCenters.length > 2000 || await store.countPublishedNeeds() > 10000) {
    throw new Problem(503, 'snapshot_limit');
  }
  const today = todayUtc(now);
  const needsByCenter = new Map();
  for (const need of await store.listNeeds()) {
    if (need.status !== 'published') continue;
    const list = needsByCenter.get(need.center_id) || [];
    list.push(syncNeed(need, today));
    needsByCenter.set(need.center_id, list);
  }
  const centers = listedCenters.map((center) => {
    const details = center.details;
    return {
      id: center.id,
      name: details.name,
      description: details.description,
      categories: details.categories,
      coordinates: details.coordinates,
      address: details.address,
      contact: details.contact,
      verificationStatus: details.verificationStatus,
      importedAtIsoDate: details.importedAtIsoDate,
      needs: needsByCenter.get(center.id) || [],
    };
  });
  const body = { catalog: { schemaVersion: 1, centers }, revision: '', updatedAt: iso(now) };
  body.revision = createHash('sha256').update(phpJson(body.catalog)).digest('hex');
  return { status: 200, body };
}

async function me({ user }) {
  return { status: 200, body: { user: publicUser(user) } };
}

async function logout({ user, tokenId, store }) {
  await store.transaction(async (tx) => {
    if (!await tx.findUserById(user.id)) throw unauthenticated();
    await tx.deleteToken(tokenId);
  });
  return { status: 204 };
}

async function myContributions({ url, user, store }) {
  const page = pageNumber(url);
  const rows = (await store.listContributions()).filter((item) => item.user_id === user.id);
  return { status: 200, body: paginate({ url, headers: { host: url.host } }, await store.page(rows, page, 25), page, 25) };
}

async function impact({ user, store }) {
  const groups = new Map();
  for (const row of await store.listContributions()) {
    if (row.user_id !== user.id || !['confirmed', 'fulfilled'].includes(row.status)) continue;
    const key = `${row.provenance}\0${row.status}`;
    const group = groups.get(key) || { provenance: row.provenance, status: row.status, contributions: 0, minutes: 0, planned_minutes: 0 };
    group.contributions += 1;
    group.minutes += row.actual_minutes || 0;
    group.planned_minutes += row.minutes || 0;
    groups.set(key, group);
  }
  return { status: 200, body: { groups: [...groups.values()].sort((a, b) => a.provenance.localeCompare(b.provenance) || a.status.localeCompare(b.status)) } };
}

async function createContribution(req) {
  const data = contributionBody(req.body);
  return mutate(req, req.store, data, async () => undefined, async (tx) => {
    const need = data.need_id ? await tx.getNeed(data.need_id) : null;
    if (data.need_id && !need) throw notFound();
    if (need) {
      if (need.status !== 'published' || (need.expires_on && need.expires_on < todayUtc(req.now))) throw new Problem(409, 'need_unavailable');
      if (!need.details.helpMethods.includes(data.method)) throw new Problem(422, 'method_unavailable');
      if (data.method === 'goods' && need.quantity_required !== null && need.quantity_committed + data.quantity > need.quantity_required) {
        throw new Problem(409, 'capacity_exceeded', { available: need.quantity_required - need.quantity_committed });
      }
      if (data.method === 'goods') {
        await tx.updateNeed(need.id, { quantity_committed: need.quantity_committed + data.quantity, version: need.version + 1, updated_at: iso(req.now) });
      }
    }
    const row = await tx.insertContribution({
      id: tx.uuid(),
      user_id: req.user.id,
      center_id: need?.center_id || null,
      need_id: need?.id || null,
      method: data.method,
      quantity: data.method === 'goods' ? data.quantity : null,
      minutes: data.method === 'time' ? data.minutes : null,
      activity: data.activity || null,
      availability: data.availability || null,
      notes: data.notes || null,
      provenance: need ? 'center_offer' : 'self_reported',
      status: 'offered',
      version: 1,
      title: need?.details.title || { es: 'Ayuda personal', en: 'Personal help' },
      quantity_unit: need ? (need.details.unit ?? null) : (data.quantity_unit ?? null),
      actual_minutes: null,
      created_at: iso(req.now),
      updated_at: iso(req.now),
    });
    await tx.insertEvent({ contribution_id: row.id, actor_id: req.user.id, from_status: null, to_status: 'offered', version: 1, created_at: iso(req.now) });
    return { status: 201, body: row };
  });
}

async function showContribution(req) {
  const row = await req.store.getContribution(req.params.contribution);
  if (!row) throw notFound();
  if (row.user_id !== req.user.id) {
    if (!row.center_id || !await req.store.isStaff(req.user.id, row.center_id)) throw forbidden();
  }
  return { status: 200, body: { contribution: row, events: await req.store.listEvents(row.id) } };
}

async function transition(req) {
  const errors = {};
  if (!['confirm', 'fulfill', 'cancel', 'complete'].includes(req.body.action)) errors.action = ['Invalid value.'];
  if (!integer(req.body.version, 1, Number.MAX_SAFE_INTEGER)) errors.version = ['Invalid value.'];
  if (req.body.actual_minutes !== undefined && !integer(req.body.actual_minutes, 1, 525600)) errors.actual_minutes = ['Invalid value.'];
  if (Object.keys(errors).length) fail(errors);
  const data = { action: req.body.action, version: req.body.version };
  if (req.body.actual_minutes !== undefined) data.actual_minutes = req.body.actual_minutes;
  const current = await req.store.getContribution(req.params.contribution);
  if (!current) throw notFound();
  return mutate(req, req.store, data, async (tx) => {
    const row = await tx.getContribution(current.id);
    if (['confirm', 'fulfill'].includes(data.action)) {
      if (!row.center_id || row.user_id === req.user.id) throw forbidden();
      await requireStaff(tx, req.user.id, row.center_id);
    } else if (row.user_id !== req.user.id) {
      throw forbidden();
    }
  }, async (tx) => {
    const row = await tx.getContribution(current.id);
    if (row.version !== data.version) throw new Problem(409, 'version_conflict', { current: row });
    const next = data.action === 'confirm' && row.status === 'offered' ? 'confirmed'
      : data.action === 'fulfill' && row.status === 'confirmed' ? 'fulfilled'
        : data.action === 'cancel' && ['offered', 'confirmed'].includes(row.status) ? 'cancelled'
          : data.action === 'complete' && row.provenance === 'self_reported' && row.status === 'offered' ? 'fulfilled'
            : null;
    if (!next) throw new Problem(409, 'invalid_transition', { current: row });
    if (next === 'cancelled' && row.need_id && row.method === 'goods') {
      const need = await tx.getNeed(row.need_id);
      await tx.updateNeed(need.id, { quantity_committed: need.quantity_committed - row.quantity, version: need.version + 1, updated_at: iso(req.now) });
    }
    if (data.actual_minutes !== undefined && (next !== 'fulfilled' || row.method !== 'time')) throw new Problem(422, 'invalid_duration');
    const before = row.status;
    const saved = await tx.updateContribution(row.id, {
      status: next,
      version: row.version + 1,
      actual_minutes: data.actual_minutes ?? row.actual_minutes,
      updated_at: iso(req.now),
    });
    await tx.insertEvent({ contribution_id: row.id, actor_id: req.user.id, from_status: before, to_status: next, version: saved.version, created_at: iso(req.now) });
    return { status: 200, body: saved };
  });
}

async function myCenters({ user, store }) {
  return { status: 200, body: { centers: (await store.staffCenters(user.id)).map((center) => ({ id: center.id, name: center.details.name })) } };
}

async function centerContributions(req) {
  const page = pageNumber(req.url);
  await requireStaff(req.store, req.user.id, req.params.center);
  const rows = (await req.store.listContributions()).filter((item) => item.center_id === req.params.center);
  return { status: 200, body: paginate(req, await req.store.page(rows, page, 25), page, 25) };
}

async function staffNeeds(req) {
  const page = pageNumber(req.url);
  await requireStaff(req.store, req.user.id, req.params.center);
  const rows = (await req.store.listNeeds()).filter((item) => item.center_id === req.params.center).map(publicNeed);
  return { status: 200, body: paginate(req, await req.store.page(rows, page, 25), page, 25) };
}

function needRecord(centerId, data, id, now) {
  return {
    id,
    center_id: centerId,
    details: { title: data.title, description: data.description, helpMethods: data.helpMethods, urgent: data.urgent, unit: data.unit },
    quantity_required: data.quantityRequired,
    quantity_committed: 0,
    expires_on: data.expiresOnIsoDate,
    status: data.status,
    version: 1,
    created_at: iso(now),
    updated_at: iso(now),
  };
}

async function createNeed(req) {
  const data = needBody(req.body, false);
  return mutate(req, req.store, data, async (tx) => requireStaff(tx, req.user.id, req.params.center), async (tx) => {
    const need = await tx.insertNeed(needRecord(req.params.center, data, tx.uuid(), req.now));
    return { status: 201, body: publicNeed(need) };
  });
}

async function updateNeed(req) {
  const existing = await req.store.getNeed(req.params.need);
  if (!existing || existing.center_id !== req.params.center) throw notFound();
  const data = needBody(req.body, true);
  return mutate(req, req.store, data, async (tx) => requireStaff(tx, req.user.id, req.params.center), async (tx) => {
    const current = await tx.getNeed(req.params.need);
    if (current.version !== data.version) throw new Problem(409, 'version_conflict', { current: publicNeed(current) });
    if (data.quantityRequired !== null && data.quantityRequired < current.quantity_committed) throw new Problem(409, 'capacity_below_commitments');
    if (current.quantity_committed > 0 && data.unit !== (current.details.unit ?? null)) throw new Problem(409, 'reserved_unit_immutable');
    const saved = await tx.updateNeed(current.id, {
      details: { title: data.title, description: data.description, helpMethods: data.helpMethods, urgent: data.urgent, unit: data.unit },
      quantity_required: data.quantityRequired,
      expires_on: data.expiresOnIsoDate,
      status: data.status,
      version: current.version + 1,
      updated_at: iso(req.now),
    });
    return { status: 200, body: publicNeed(saved) };
  });
}

async function account({ user, store, now }) {
  const contributions = (await store.listContributions()).filter((item) => item.user_id === user.id);
  const visits = await store.listVisits(user.id);
  if (contributions.length > 5000 || visits.length > 5000) throw new Problem(503, 'snapshot_limit');
  return { status: 200, body: { user: publicUser(user), contributions, visits, updatedAt: iso(now) } };
}

async function visit(req) {
  const data = visitBody(req.body, req.now);
  return mutate(req, req.store, data, async () => undefined, async (tx) => {
    if (!await tx.getCenter(data.center_id)) throw notFound();
    const row = await tx.insertVisit({
      id: tx.uuid(),
      user_id: req.user.id,
      center_id: data.center_id,
      occurred_at: data.occurred_at,
      received_at: iso(req.now),
    });
    return { status: 201, body: row };
  });
}

async function apply(req) {
  if (!req.config._applicationBuckets) req.config._applicationBuckets = new Map();
  rateLimit(req.config._applicationBuckets, `center-applications:${req.user.id}`, req.config.limits?.applications || 5, req.now.getTime());
  const data = applicationBody(req.body);
  const fingerprint = createHash('sha256').update(phpJson(data)).digest('hex');
  return mutate(req, req.store, data, async () => undefined, async (tx) => {
    const existing = await tx.findApplicationByFingerprint(req.user.id, fingerprint);
    if (existing) return { status: 200, body: { application: applicationSummary(existing) } };
    const row = await tx.insertApplication({
      id: tx.uuid(),
      user_id: req.user.id,
      contact_email: req.user.email,
      details: data,
      fingerprint,
      status: 'pending',
      consent_version: 'site-application-v1',
      created_at: iso(req.now),
      updated_at: iso(req.now),
    });
    return { status: 201, body: { application: applicationSummary(row) } };
  });
}

async function myApplications(req) {
  const page = pageNumber(req.url);
  const rows = (await req.store.listApplications(req.user.id)).map(applicationSummary);
  const paged = await req.store.page(rows, page, 10);
  return {
    status: 200,
    body: { data: paged.rows, current_page: page, last_page: Math.max(1, Math.ceil(paged.total / 10)), per_page: 10 },
  };
}

function matchRoute(path, method) {
  let methodMismatch = false;
  for (const [routeMethod, template, auth, handler] of routes) {
    const names = [];
    const pattern = template.replace(/\{([^}]+)\}/g, (_, name) => {
      names.push(name);
      return '([^/]+)';
    });
    const found = new RegExp(`^${pattern}$`).exec(path);
    if (!found) continue;
    if (routeMethod !== method) {
      methodMismatch = true;
      continue;
    }
    const concrete = template.replace(/\{([^}]+)\}/g, (_, name) => found[names.indexOf(name) + 1]);
    return {
      auth,
      handler,
      path: concrete,
      params: Object.fromEntries(names.map((name, index) => [name, decodeURIComponent(found[index + 1])])),
    };
  }
  if (methodMismatch) throw new HttpError(405, 'method_not_allowed');
  return null;
}
