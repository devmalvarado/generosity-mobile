# Local backend runtime

The runnable backend on this branch is now [PHP/Laravel with MariaDB](../backend/README.md). Follow that guide for Docker Compose startup, API routes, account verification, staff provisioning and tests. See [the architecture decision](backend-php-mysql.md) and [Hostinger deployment gates](hostinger-backend.md).

The earlier Kotlin directory-journal service is preserved at checkpoint `36eab89` on branch `codex/mockup-backend-kotlin`. Its `:service:run`, `:service:test` and old backup format apply only to that branch. `codex/backend-php-mysql` removes that Gradle module while retaining the Kotlin Multiplatform mobile/shared code.

PHP starts with an empty SQL schema; there is no silent conversion of Kotlin mock data. Native apps and the center portal are not yet wired to this API. No production server or database has been changed.
