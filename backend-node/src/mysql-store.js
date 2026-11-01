import { randomUUID } from 'node:crypto';
import { iso } from './store.js';

// Same tables as the PHP migrations. No cloud SDK: DATABASE_URL can point at
// local MySQL, Amazon RDS, or Azure Database for MySQL without code changes.
export async function createMysqlStore(databaseUrl) {
  const mysql = await import('mysql2/promise');
  const pool = mysql.createPool(databaseUrl);
  const q = (conn, sql, params = []) => conn.execute(sql, params).then(([rows]) => rows);

  function bind(conn) {
    return {
      uuid: () => randomUUID(),
      async findActiveUserByEmail(email) {
        const rows = await q(conn, 'select * from users where email = ? and deactivated_at is null limit 1', [email]);
        return rows[0] ? user(rows[0]) : null;
      },
      async findUserByEmailAny(email) {
        const rows = await q(conn, 'select * from users where email = ? limit 1', [email]);
        return rows[0] ? user(rows[0]) : null;
      },
      async findUserById(id, activeOnly = true) {
        const rows = await q(conn, `select * from users where id = ? ${activeOnly ? 'and deactivated_at is null' : ''} limit 1`, [id]);
        return rows[0] ? user(rows[0]) : null;
      },
      async insertUser(fields) {
        const now = iso(fields.created_at);
        const [result] = await conn.execute(
          'insert into users (name, email, password, policy_version, policy_accepted_at, adult_confirmed_at, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?)',
          [fields.name, fields.email, fields.password, fields.policy_version, now, now, now, now],
        );
        return this.findUserById(result.insertId);
      },
      async updateUser(id, patch) {
        const current = await this.findUserById(id, false);
        const next = { ...current, ...patch, updated_at: iso(patch.updated_at || new Date()) };
        await q(conn, 'update users set name = ?, email = ?, password = ?, email_verified_at = ?, remember_token = ?, deactivated_at = ?, updated_at = ? where id = ?', [
          next.name, next.email, next.password, next.email_verified_at, next.remember_token, next.deactivated_at, next.updated_at, id,
        ]);
        return this.findUserById(id, false);
      },
      async insertToken(fields) {
        const expires = iso(fields.expiresAt);
        const [result] = await conn.execute(
          'insert into personal_access_tokens (tokenable_type, tokenable_id, name, token, abilities, expires_at, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?)',
          ['App\\Models\\User', fields.userId, fields.name, fields.hash, '["*"]', expires, expires, expires],
        );
        return { id: result.insertId, user_id: fields.userId, token: fields.hash, expires_at: expires };
      },
      async findToken(id, hash, now) {
        const rows = await q(conn, 'select * from personal_access_tokens where id = ? and token = ? limit 1', [id, hash]);
        const row = rows[0];
        if (!row || new Date(row.expires_at).getTime() <= now.getTime()) return null;
        return { id: row.id, user_id: row.tokenable_id, token: row.token, expires_at: iso(row.expires_at) };
      },
      async deleteToken(id) {
        await q(conn, 'delete from personal_access_tokens where id = ?', [id]);
      },
      async deleteUserTokens(userId) {
        await q(conn, 'delete from personal_access_tokens where tokenable_id = ? and tokenable_type = ?', [userId, 'App\\Models\\User']);
      },
      async deleteUserHandoffs(userId) {
        await q(conn, 'delete from auth_handoffs where user_id = ?', [userId]);
      },
      async listCenters() {
        return (await q(conn, 'select * from centers order by id')).map(center);
      },
      async getCenter(id) {
        const rows = await q(conn, 'select * from centers where id = ? limit 1', [id]);
        return rows[0] ? center(rows[0]) : null;
      },
      async insertCenter(row) {
        await q(conn, 'insert into centers (id, details, version, created_at, updated_at) values (?, ?, ?, ?, ?)', [
          row.id, JSON.stringify(row.details), row.version, row.created_at, row.updated_at,
        ]);
        return row;
      },
      async isStaff(userId, centerId) {
        const rows = await q(conn, 'select 1 from center_memberships where user_id = ? and center_id = ? limit 1', [userId, centerId]);
        return rows.length > 0;
      },
      async addMembership(userId, centerId) {
        await q(conn, 'insert ignore into center_memberships (user_id, center_id) values (?, ?)', [userId, centerId]);
      },
      async staffCenters(userId) {
        return (await q(conn, 'select centers.* from centers join center_memberships on centers.id = center_memberships.center_id where center_memberships.user_id = ? order by centers.id', [userId])).map(center);
      },
      async listNeeds() {
        return (await q(conn, 'select * from needs order by id')).map(need);
      },
      async countPublishedNeeds() {
        const rows = await q(conn, "select count(*) as total from needs where status = 'published'");
        return Number(rows[0].total);
      },
      async getNeed(id) {
        const rows = await q(conn, 'select * from needs where id = ? limit 1', [id]);
        return rows[0] ? need(rows[0]) : null;
      },
      async insertNeed(row) {
        await q(conn, 'insert into needs (id, center_id, details, status, quantity_required, quantity_committed, expires_on, version, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)', [
          row.id, row.center_id, JSON.stringify(row.details), row.status, row.quantity_required, row.quantity_committed, row.expires_on, row.version, row.created_at, row.updated_at,
        ]);
        return row;
      },
      async updateNeed(id, patch) {
        const current = { ...await this.getNeed(id), ...patch };
        await q(conn, 'update needs set details = ?, status = ?, quantity_required = ?, quantity_committed = ?, expires_on = ?, version = ?, updated_at = ? where id = ?', [
          JSON.stringify(current.details), current.status, current.quantity_required, current.quantity_committed, current.expires_on, current.version, current.updated_at, id,
        ]);
        return this.getNeed(id);
      },
      async insertContribution(row) {
        await q(conn, `insert into contributions (id, user_id, center_id, need_id, method, quantity, minutes, activity, availability, notes, provenance, status, version, title, quantity_unit, actual_minutes, created_at, updated_at)
          values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`, [
          row.id, row.user_id, row.center_id, row.need_id, row.method, row.quantity, row.minutes, row.activity, row.availability, row.notes,
          row.provenance, row.status, row.version, JSON.stringify(row.title), row.quantity_unit, row.actual_minutes, row.created_at, row.updated_at,
        ]);
        return row;
      },
      async getContribution(id) {
        const rows = await q(conn, 'select * from contributions where id = ? limit 1', [id]);
        return rows[0] ? contribution(rows[0]) : null;
      },
      async updateContribution(id, patch) {
        const current = { ...await this.getContribution(id), ...patch };
        await q(conn, 'update contributions set status = ?, version = ?, actual_minutes = ?, updated_at = ? where id = ?', [
          current.status, current.version, current.actual_minutes, current.updated_at, id,
        ]);
        return this.getContribution(id);
      },
      async listContributions() {
        return (await q(conn, 'select * from contributions order by created_at desc, id')).map(contribution);
      },
      async insertEvent(row) {
        const [result] = await conn.execute(
          'insert into contribution_events (contribution_id, actor_id, from_status, to_status, version, created_at) values (?, ?, ?, ?, ?, ?)',
          [row.contribution_id, row.actor_id, row.from_status, row.to_status, row.version, row.created_at],
        );
        return { id: result.insertId, ...row };
      },
      async listEvents(contributionId) {
        return q(conn, 'select * from contribution_events where contribution_id = ? order by version', [contributionId]);
      },
      async insertVisit(row) {
        await q(conn, 'insert into visit_signals (id, user_id, center_id, occurred_at, received_at) values (?, ?, ?, ?, ?)', [
          row.id, row.user_id, row.center_id, row.occurred_at, row.received_at,
        ]);
        return row;
      },
      async listVisits(userId) {
        return q(conn, 'select id, user_id, center_id, occurred_at, received_at from visit_signals where user_id = ? order by received_at, id', [userId]);
      },
      async insertApplication(row) {
        await q(conn, 'insert into center_applications (id, user_id, contact_email, details, fingerprint, status, consent_version, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)', [
          row.id, row.user_id, row.contact_email, JSON.stringify(row.details), row.fingerprint, row.status, row.consent_version, row.created_at, row.updated_at,
        ]);
        return row;
      },
      async findApplicationByFingerprint(userId, fingerprint) {
        const rows = await q(conn, 'select * from center_applications where user_id = ? and fingerprint = ? limit 1', [userId, fingerprint]);
        return rows[0] ? application(rows[0]) : null;
      },
      async listApplications(userId) {
        return (await q(conn, 'select * from center_applications where user_id = ? order by created_at desc, id', [userId])).map(application);
      },
      async getOperation(userId, key) {
        const rows = await q(conn, 'select * from operations where user_id = ? and `key` = ? limit 1', [userId, key]);
        if (!rows[0]) return null;
        return { ...rows[0], response: typeof rows[0].response === 'string' ? JSON.parse(rows[0].response) : rows[0].response };
      },
      async insertOperation(row) {
        await q(conn, 'insert into operations (user_id, `key`, request_hash, response, status, created_at) values (?, ?, ?, ?, ?, ?)', [
          row.user_id, row.key, row.request_hash, JSON.stringify(row.response), row.status, row.created_at,
        ]);
        return row;
      },
      async getHandoff(id) {
        const rows = await q(conn, 'select * from auth_handoffs where id = ? limit 1', [id]);
        return rows[0] || null;
      },
      async deleteHandoff(id) {
        await q(conn, 'delete from auth_handoffs where id = ?', [id]);
      },
      async insertHandoff(row) {
        await q(conn, 'insert into auth_handoffs (id, user_id, client, challenge, expires_at) values (?, ?, ?, ?, ?)', [
          row.id, row.user_id, row.client, row.challenge, row.expires_at,
        ]);
      },
      async saveReset(row) {
        await q(conn, 'replace into password_reset_tokens (email, token, created_at) values (?, ?, ?)', [row.email, row.token, iso(row.created_at)]);
      },
      async getReset(email) {
        const rows = await q(conn, 'select * from password_reset_tokens where email = ? limit 1', [email]);
        return rows[0] ? { ...rows[0], created_at: new Date(rows[0].created_at).getTime() } : null;
      },
      async deleteReset(email) {
        await q(conn, 'delete from password_reset_tokens where email = ?', [email]);
      },
      page(rows, page, perPage) {
        const total = rows.length;
        const last = Math.max(1, Math.ceil(total / perPage));
        return { rows: rows.slice((page - 1) * perPage, page * perPage), total, perPage, page, last };
      },
    };
  }

  return {
    kind: 'mysql',
    async health() {
      const rows = await q(pool, 'select count(*) as total from migrations');
      return Number(rows[0].total) > 0;
    },
    async transaction(fn) {
      const conn = await pool.getConnection();
      try {
        await conn.beginTransaction();
        const result = await fn(bind(conn));
        await conn.commit();
        return result;
      } catch (error) {
        await conn.rollback();
        throw error;
      } finally {
        conn.release();
      }
    },
    ...bind(pool),
  };
}

function json(value) {
  if (value === null || value === undefined) return null;
  return typeof value === 'string' ? JSON.parse(value) : value;
}

function user(row) {
  return {
    ...row,
    id: Number(row.id),
    email_verified_at: row.email_verified_at ? iso(row.email_verified_at) : null,
    created_at: iso(row.created_at),
    updated_at: iso(row.updated_at),
    deactivated_at: row.deactivated_at ? iso(row.deactivated_at) : null,
  };
}

function center(row) {
  return { ...row, details: json(row.details), version: Number(row.version), updated_at: iso(row.updated_at) };
}

function need(row) {
  return {
    ...row,
    details: json(row.details),
    quantity_required: row.quantity_required === null ? null : Number(row.quantity_required),
    quantity_committed: Number(row.quantity_committed),
    expires_on: row.expires_on ? String(row.expires_on).slice(0, 10) : null,
    version: Number(row.version),
    updated_at: iso(row.updated_at),
  };
}

function contribution(row) {
  return {
    ...row,
    user_id: Number(row.user_id),
    quantity: row.quantity === null ? null : Number(row.quantity),
    minutes: row.minutes === null ? null : Number(row.minutes),
    actual_minutes: row.actual_minutes === null ? null : Number(row.actual_minutes),
    version: Number(row.version),
    title: json(row.title),
    created_at: iso(row.created_at),
    updated_at: iso(row.updated_at),
  };
}

function application(row) {
  return { ...row, user_id: Number(row.user_id), details: json(row.details), created_at: iso(row.created_at) };
}
