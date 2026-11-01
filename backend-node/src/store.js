import { randomUUID } from 'node:crypto';

export function iso(value) {
  return new Date(value).toISOString();
}

function slicePage(rows, page, perPage) {
  const total = rows.length;
  const last = Math.max(1, Math.ceil(total / perPage));
  const start = (page - 1) * perPage;
  return { rows: rows.slice(start, start + perPage), total, perPage, page, last };
}

export function publicUser(user) {
  return {
    id: user.id,
    name: user.name,
    email: user.email,
    email_verified_at: user.email_verified_at,
    created_at: user.created_at,
    updated_at: user.updated_at,
  };
}

export function createMemoryStore() {
  const db = {
    users: [],
    tokens: [],
    centers: [],
    memberships: [],
    needs: [],
    contributions: [],
    events: [],
    visits: [],
    applications: [],
    operations: [],
    handoffs: [],
    resets: [],
    nextUser: 1,
    nextToken: 1,
    nextEvent: 1,
  };
  let chain = Promise.resolve();
  const repo = {
    kind: 'memory',
    async health() {
      return true;
    },
    transaction(fn) {
      const run = chain.then(() => fn(repo));
      chain = run.then(() => undefined, () => undefined);
      return run;
    },
    uuid: () => randomUUID(),
    findActiveUserByEmail(email) {
      return db.users.find((user) => user.email === email && !user.deactivated_at) || null;
    },
    findUserByEmailAny(email) {
      return db.users.find((user) => user.email === email) || null;
    },
    findUserById(id, activeOnly = true) {
      return db.users.find((user) => user.id === id && (!activeOnly || !user.deactivated_at)) || null;
    },
    insertUser(fields) {
      const now = iso(fields.created_at);
      const user = {
        id: db.nextUser++,
        name: fields.name,
        email: fields.email,
        password: fields.password,
        email_verified_at: null,
        remember_token: null,
        deactivated_at: null,
        policy_version: fields.policy_version,
        policy_accepted_at: fields.policy_accepted_at,
        adult_confirmed_at: fields.adult_confirmed_at,
        created_at: now,
        updated_at: now,
      };
      db.users.push(user);
      return user;
    },
    updateUser(id, patch) {
      const user = db.users.find((item) => item.id === id);
      Object.assign(user, patch, { updated_at: iso(patch.updated_at || new Date()) });
      return user;
    },
    insertToken(fields) {
      const row = {
        id: db.nextToken++,
        user_id: fields.userId,
        name: fields.name,
        token: fields.hash,
        expires_at: iso(fields.expiresAt),
      };
      db.tokens.push(row);
      return row;
    },
    findToken(id, hash, now) {
      const row = db.tokens.find((item) => item.id === id && item.token === hash);
      if (!row || new Date(row.expires_at).getTime() <= now.getTime()) {
        return null;
      }
      return row;
    },
    deleteToken(id) {
      db.tokens = db.tokens.filter((item) => item.id !== id);
    },
    deleteUserTokens(userId) {
      db.tokens = db.tokens.filter((item) => item.user_id !== userId);
    },
    deleteUserHandoffs(userId) {
      db.handoffs = db.handoffs.filter((item) => item.user_id !== userId);
    },
    listCenters() {
      return [...db.centers].sort((a, b) => a.id.localeCompare(b.id));
    },
    getCenter(id) {
      return db.centers.find((item) => item.id === id) || null;
    },
    insertCenter(center) {
      db.centers.push(center);
      return center;
    },
    isStaff(userId, centerId) {
      return db.memberships.some((item) => item.user_id === userId && item.center_id === centerId);
    },
    addMembership(userId, centerId) {
      db.memberships.push({ user_id: userId, center_id: centerId });
    },
    staffCenters(userId) {
      const ids = new Set(db.memberships.filter((item) => item.user_id === userId).map((item) => item.center_id));
      return db.centers.filter((item) => ids.has(item.id)).sort((a, b) => a.id.localeCompare(b.id));
    },
    listNeeds() {
      return [...db.needs].sort((a, b) => a.id.localeCompare(b.id));
    },
    countPublishedNeeds() {
      return db.needs.filter((item) => item.status === 'published').length;
    },
    getNeed(id) {
      return db.needs.find((item) => item.id === id) || null;
    },
    insertNeed(need) {
      db.needs.push(need);
      return need;
    },
    updateNeed(id, patch) {
      const need = db.needs.find((item) => item.id === id);
      Object.assign(need, patch);
      return need;
    },
    insertContribution(row) {
      db.contributions.push(row);
      return row;
    },
    getContribution(id) {
      return db.contributions.find((item) => item.id === id) || null;
    },
    updateContribution(id, patch) {
      const row = db.contributions.find((item) => item.id === id);
      Object.assign(row, patch);
      return row;
    },
    listContributions() {
      return [...db.contributions].sort((a, b) => b.created_at.localeCompare(a.created_at) || a.id.localeCompare(b.id));
    },
    insertEvent(row) {
      const event = { id: db.nextEvent++, ...row };
      db.events.push(event);
      return event;
    },
    listEvents(contributionId) {
      return db.events.filter((item) => item.contribution_id === contributionId).sort((a, b) => a.version - b.version);
    },
    insertVisit(row) {
      db.visits.push(row);
      return row;
    },
    listVisits(userId) {
      return db.visits.filter((item) => item.user_id === userId).sort((a, b) => a.received_at.localeCompare(b.received_at) || a.id.localeCompare(b.id));
    },
    insertApplication(row) {
      db.applications.push(row);
      return row;
    },
    findApplicationByFingerprint(userId, fingerprint) {
      return db.applications.find((item) => item.user_id === userId && item.fingerprint === fingerprint) || null;
    },
    listApplications(userId) {
      return db.applications.filter((item) => item.user_id === userId).sort((a, b) => b.created_at.localeCompare(a.created_at) || a.id.localeCompare(b.id));
    },
    getOperation(userId, key) {
      return db.operations.find((item) => item.user_id === userId && item.key === key) || null;
    },
    insertOperation(row) {
      db.operations.push(row);
      return row;
    },
    getHandoff(id) {
      return db.handoffs.find((item) => item.id === id) || null;
    },
    deleteHandoff(id) {
      db.handoffs = db.handoffs.filter((item) => item.id !== id);
    },
    insertHandoff(row) {
      db.handoffs.push(row);
    },
    saveReset(row) {
      db.resets = db.resets.filter((item) => item.email !== row.email);
      db.resets.push(row);
    },
    getReset(email) {
      return db.resets.find((item) => item.email === email) || null;
    },
    deleteReset(email) {
      db.resets = db.resets.filter((item) => item.email !== email);
    },
    page: slicePage,
  };
  for (const [key, value] of Object.entries(repo)) {
    if (typeof value === 'function' && key !== 'transaction' && key !== 'uuid') {
      repo[key] = (...args) => Promise.resolve(value(...args));
    }
  }
  return repo;
}
