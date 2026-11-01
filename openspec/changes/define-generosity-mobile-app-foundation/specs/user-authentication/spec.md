## Purpose
Define real account entry and session lifecycle across both apps and the backend while preserving anonymous discovery and preventing fabricated identities.

The configured first-party provider for the PHP implementation is Laravel
framework authentication with server-only password hashes, verified email and
Sanctum bearer tokens. This selects an identity implementation without changing
mobile session-restoration, recovery or current-user authorization requirements.

## ADDED Requirements

### Requirement: Provider-Backed Account Entry
Registration and sign-in SHALL use the configured identity provider. A registered state SHALL require validated provider credentials and a backend account identifier. Provider-required verification SHALL complete before protected operations are enabled. A typed name, fixed sample account or client-supplied identifier SHALL NOT authenticate a user.

#### Scenario: Complete registration
- **WHEN** the provider accepts registration and required verification and the backend validates the resulting identity
- **THEN** the app enters registered mode with the actual user's profile

#### Scenario: Failed or cancelled entry
- **WHEN** identity entry fails, is cancelled or cannot reach the provider
- **THEN** the app shows a localized outcome, keeps anonymous discovery available and does not sign in as a sample user

### Requirement: Session Restoration and Expiry
The app SHALL securely restore and renew valid sessions and handle expired or revoked credentials. Cached data SHALL NOT prove current server authorization. Offline access to previously restored local history SHALL be labeled offline; protected writes SHALL wait for successful reauthorization.

#### Scenario: Expired session
- **WHEN** renewal fails because the session is expired or revoked
- **THEN** protected actions require sign-in, queued writes stop and public browsing remains available

#### Scenario: Offline restart with an existing local account
- **WHEN** a previously authenticated device restarts without connectivity
- **THEN** it can show that account's isolated cached history without claiming the backend has validated a new session

### Requirement: Server-Side Authorization
The backend SHALL derive account identity from validated credentials, reject unauthorized private reads/writes, and enforce center membership for center actions. Authentication SHALL NOT grant center privileges automatically. Client-supplied user or center identifiers SHALL NOT override authorization.

#### Scenario: Access another user's contribution
- **WHEN** an authenticated user requests a contribution belonging to another account without an authorized center role
- **THEN** the backend denies access without revealing its private contents

#### Scenario: Forged center role
- **WHEN** a mobile request claims a center role that the account does not hold
- **THEN** the backend rejects confirmation or fulfillment regardless of client state

### Requirement: Sign-Out and Recovery
Sign-out SHALL remove local credentials, revoke the session when supported, suspend account queues and clear private visible state. Users SHALL have access to the provider's account-recovery flow without the app storing passwords or recovery secrets. Sign-out SHALL not silently delete unsent private work.

#### Scenario: Sign out with unsent work
- **WHEN** the user signs out with pending account operations
- **THEN** the app warns about suspended work, hides that account and returns to guest-capable welcome

#### Scenario: Recover account access
- **WHEN** the user chooses account recovery from sign-in
- **THEN** the app opens the configured provider flow and does not fabricate a successful reset

### Requirement: Safe Session Diagnostics
Credentials, passwords and recovery codes SHALL NOT be recorded in logs, analytics, URLs controlled by the app or public crash context. Identity errors SHALL be localized without exposing secrets or other users' account details.

#### Scenario: Authentication request fails
- **WHEN** a failed request is logged for troubleshooting
- **THEN** diagnostics contain a safe error category or correlation identifier but no credential values
