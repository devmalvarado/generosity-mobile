# Local service

Date: 2026-10-31. This is the local runtime for foundation task 11.2. It is not a hosting selection, a database vendor or an identity provider. Those remain open in task 11.1 and tasks 7.8–7.10.

## What it is

A Kotlin/JVM process with a versioned directory store. Identity, payment and notification delivery are ports. The defaults are unconfigured: sign-in returns unavailable, payments stay unavailable, and no message is sent. A test double may accept a credential only when a test constructs it. The process does not read production credentials and does not load a demo account.

The store is one directory:

- `schema-version` is `1`. A different version refuses to start.
- `catalog.json` is replaced only after `CatalogJson` accepts it. A rejected import leaves the previous file.
- `catalog-revision` increases only after an accepted import. A rejected import leaves the revision and the previous catalog unchanged.
- `operations.log` is the append-only journal. Restart replays it onto the imported catalog and does not change `catalog.json`. The same operation id with the same content is acknowledged once. The same id with different content is rejected.
- `diagnostics.log` stores event codes only. Credentials and configured secret values are not written there or into health output.
- A snapshot is a copy of that directory. Restore replaces the store from a snapshot of schema version 1. Another schema version refuses to start. This is not a database-vendor backup product.

No production rate-limit threshold is selected. The default allowance is unconfigured, so a protected write stays unavailable instead of using a guessed limit. A test may inject an explicit allowance. Staff confirmation is likewise unavailable until a test or a future operator port assigns a center. Neither choice selects an identity provider.

The bundled demonstration catalog can be imported explicitly. It is still a labeled fixture, not a published center source.

## Start

From the repository root, with no provider configured:

```bash
./gradlew :service:run
./gradlew :service:test
```

`GENEROSITY_DATA` selects the directory. The default is `build/local-service`. A ready process prints `identity=unconfigured` and `payments=unavailable`.
