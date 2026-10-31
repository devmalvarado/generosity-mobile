## Purpose
Preserve mobile state across restarts and network failures while keeping account data isolated and reconciling changes with the authoritative backend.

## ADDED Requirements

### Requirement: Durable State Restoration
Both apps SHALL restore the active profile cache, contribution history, language choice, notification preferences, reminders, visit records and unsent operations after process termination. Saved data SHALL be loaded before showing an empty state. Session credentials SHALL use platform-protected storage, not general preferences or logs.

#### Scenario: Restart after a successful save
- **WHEN** the user restarts after saving an offer, reminder or language change
- **THEN** the saved data and pending synchronization state are restored for the same account

#### Scenario: Storage failure
- **WHEN** a local save fails
- **THEN** the app reports the failure and does not show a durable-save success message or discard the user's input

### Requirement: Account Isolation
Private caches and queued writes SHALL belong to a stable authenticated account. Signing out or switching accounts SHALL hide the previous account's data and stop its background writes and private notifications. The app SHALL NOT relabel one account's pending operations as another account's operations.

#### Scenario: Switch from account A to B
- **WHEN** B signs in on a device containing A's pending offers
- **THEN** B cannot see or submit A's offers, and A's queue remains suspended until A authenticates again

### Requirement: Durable Idempotent Outbox
Offline contributions, cancellations and registered visit submissions SHALL retain a stable operation identifier and an explicit pending state. Retrying after restart or a lost response SHALL not create duplicates. A pending operation SHALL NOT claim server acceptance, reserved capacity or center confirmation.

#### Scenario: Connection fails after server acceptance
- **WHEN** the app retries an operation whose response was lost
- **THEN** the server returns the existing outcome for that operation and the app keeps one history entry

#### Scenario: Offline offer loses the capacity race
- **WHEN** synchronization rejects a queued goods offer because capacity is no longer available
- **THEN** the app shows the rejection, removes only its provisional projection and preserves the user's entered details for correction

### Requirement: Authoritative Reconciliation
Server identifiers, versions and acknowledged lifecycle states SHALL override stale local projections. Synchronization SHALL merge history by stable identifier, apply removals and reject conflicting writes explicitly. Pending, rejected and acknowledged records SHALL remain distinguishable in the UI.

#### Scenario: Center completes an offer on another device
- **WHEN** the user's app next synchronizes
- **THEN** its existing entry changes to the newer server state without adding a second contribution or double-counting impact

### Requirement: Safe Upgrade and Recovery
Local schema upgrades SHALL preserve supported saved records and pending operation identifiers. Unreadable storage SHALL produce a recoverable error, not silent deletion. Only reconstructible public catalog cache SHALL be automatically replaced; private unsent work SHALL require an explicit recovery decision.

#### Scenario: Upgrade with pending work
- **WHEN** an app upgrade changes local storage format
- **THEN** migration tests demonstrate that account ownership, saved preferences and pending operations survive
