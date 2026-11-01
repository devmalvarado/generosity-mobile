# Generosity API, Node alternative

Portable HTTP service for the existing Generosity OpenAPI contract at
`backend/openapi.json`. It is an alternative to the PHP/Laravel API, not a
replacement and not a deployment. The PHP service remains the published API.

The process is stateless. Configuration comes only from the environment. It has
no AWS SDK and no Azure SDK. A later move to either cloud is a container plus a
MySQL-compatible database URL, not a rewrite:

- Amazon ECS, App Runner or a similar container host, with Amazon RDS for MySQL
- Azure Container Apps or App Service, with Azure Database for MySQL

Do not create those resources from this repository. Payments stay disabled.
Google and Apple credentials are not read and are not invented. Mail is a port:
this process records the message for a later adapter and does not send it.
Browser cookie sessions stay on the PHP host; this service implements the
bearer contract in OpenAPI.

## Contract

Every OpenAPI operation under `/api/v1` is registered at startup. Review,
rejection and conversion of a center application are not in that contract, so
this service does not add them. A submitted application stays private and
pending. It does not create a catalog center, a membership or a verification.

## Run

```bash
cd backend-node
npm install
npm test
npm start
```

Without `DATABASE_URL` the process uses a memory store and says so. That store
is for local checks, not for a cloud host. Production refuses to start unless
`DATABASE_URL`, `APP_KEY` and an `https://` `APP_URL` are set. Point
`DATABASE_URL` at the same schema the PHP migrations already create. This
service does not run migrations and does not read the PHP `.env`.

Build the container from the repository root, still without provisioning a cloud
account:

```bash
docker build -f backend-node/Dockerfile -t generosity-api-node .
```
