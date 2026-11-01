import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');

export function loadContract(env = process.env) {
  const path = env.CONTRACT_PATH || resolve(root, '../backend/openapi.json');
  const spec = JSON.parse(readFileSync(path, 'utf8'));
  if (spec.openapi !== '3.1.0' || spec.info?.title !== 'Generosity API') {
    throw new Error('OpenAPI contract is not the Generosity API');
  }
  return { path, spec };
}

export function loadConfig(env = process.env) {
  const contract = loadContract(env);
  const nodeEnv = env.NODE_ENV || 'development';
  const databaseUrl = env.DATABASE_URL || '';
  const appKey = env.APP_KEY || '';
  const appUrl = (env.APP_URL || 'http://127.0.0.1:8088').replace(/\/$/, '');
  if (nodeEnv === 'production' && (!databaseUrl || !appKey || !appUrl.startsWith('https://'))) {
    throw new Error('Production requires DATABASE_URL, APP_KEY and an https APP_URL');
  }
  return {
    nodeEnv,
    port: Number(env.PORT || 8088),
    appUrl,
    appKey: appKey || 'test-only-key',
    databaseUrl,
    allowedOrigins: (env.ALLOWED_ORIGINS || '').split(',').map((item) => item.trim()).filter(Boolean),
    verificationSeconds: 60 * 60,
    resetSeconds: 60 * 60,
    tokenDays: 7,
    policyVersion: '2026-11-01',
    contract,
    now: () => new Date(),
  };
}
