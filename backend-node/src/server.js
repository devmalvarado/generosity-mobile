import { createServer } from 'node:http';
import { createApp } from './app.js';
import { loadConfig } from './config.js';
import { createMysqlStore } from './mysql-store.js';
import { createMemoryStore } from './store.js';

const config = loadConfig();
const store = config.databaseUrl ? await createMysqlStore(config.databaseUrl) : createMemoryStore();
if (!config.databaseUrl) {
  console.error('durable_store_unconfigured');
}
const app = createApp({ config, store, mail: { push() {} } });
const server = createServer((req, res) => {
  app.handle(req, res).catch((error) => {
    console.error('backend_failure', error?.name || 'Error');
    if (!res.headersSent) {
      res.writeHead(500, { 'content-type': 'application/json; charset=utf-8', 'cache-control': 'no-store' });
    }
    res.end(JSON.stringify({ error: { code: 'service_unavailable', details: {} } }));
  });
});
server.listen(config.port, () => {
  console.log(`listening ${config.port}`);
});
