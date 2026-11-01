## Purpose
Define real account entry and session lifecycle across both apps and the backend while preserving anonymous discovery and preventing fabricated identities.

The configured first-party provider for the PHP implementation is Laravel
framework authentication with server-only password hashes, verified email and
Sanctum bearer tokens for native apps and CSRF-protected sessions for the web.
Google and Apple authorization-code identity entry is layered onto the same
account store. Live provider credentials are pending; see the implementation audit.
Facebook entry and feature-specific mobile-number verification below are planned,
not implemented. A verified social identity, email and mobile number are separate proofs.

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
Passwords, bearer tokens and provider tokens SHALL NOT be recorded in logs, analytics, app URLs or public crash context. Protocol authorization callbacks may carry short-lived codes; native handoff codes SHALL be single-use and PKCE-bound. Callback queries and form bodies SHALL be omitted from access logs. Identity errors SHALL be localized without exposing secrets or other users' account details.

#### Scenario: Authentication request fails
- **WHEN** a failed request is logged for troubleshooting
- **THEN** diagnostics contain a safe error category or correlation identifier but no credential values

### Requirement: Generosity-Only Social Identity
Generosity SHALL offer Google, Sign in with Apple and Facebook for web, Android and iOS alongside verified-email authentication. All clients SHALL resolve the same provider subject to the same backend account. Other institute platforms and Microsoft SHALL remain outside this scope. Missing provider credentials SHALL disable that provider without blocking guest discovery.

#### Scenario: Provider not configured
- **WHEN** Google, Apple or Facebook credentials are absent
- **THEN** its control is disabled with a localized explanation and no demonstration identity is substituted

#### Scenario: Same provider on another client
- **WHEN** a person signs in with the same validated provider subject on another Generosity client
- **THEN** the backend resolves the existing account, issues a separate client session and grants no additional center roles

### Requirement: Verified Social Authorization
The backend SHALL exchange codes at fixed Google/Apple endpoints and verify allowed algorithm, signature, issuer, audience, expiry, issue time, subject and nonce. Attempts SHALL be browser-bound, expire after five minutes and be consumed once. Apple's form-post callback SHALL preserve browser binding despite cross-site cookie rules.

#### Scenario: Invalid identity proof
- **WHEN** the signature, issuer, audience, nonce, browser binding or attempt expiry is invalid
- **THEN** the backend rejects authentication without creating an account, linking an identity or granting a session

### Requirement: Stable Identity and Explicit Linking
Identity SHALL use provider and stable subject, never email or phone alone. New accounts SHALL require email verified by a trusted provider claim or Generosity's email-verification flow before protected operations. Apple private-relay addresses SHALL be accepted. Linking SHALL require an explicit action in a Generosity session authenticated within five minutes. Identity ownership SHALL NOT move between accounts.

#### Scenario: Existing email collision
- **WHEN** a new provider subject reports an email already used by an account
- **THEN** the backend requires the existing sign-in method followed by explicit linking and never silently merges accounts

#### Scenario: Provider belongs to another account
- **WHEN** a user attempts to link an identity already owned by another account
- **THEN** the backend refuses the link and preserves both accounts

### Requirement: Facebook Authorization
Facebook SHALL reuse hosted browser entry, Generosity sessions and native PKCE handoff. The backend SHALL exchange authorization codes at fixed provider endpoints and validate the resulting identity for the configured Meta app. Attempts SHALL be browser-bound, expire after five minutes and be consumed once. Client-supplied profile IDs or tokens for another app SHALL NOT authenticate. Facebook's OAuth flow SHALL NOT be assumed to return Google-style OIDC claims.

#### Scenario: Invalid or cancelled Facebook authorization
- **WHEN** authorization is cancelled, state or browser binding fails, the attempt is replayed or expired, or identity cannot be validated for the configured app
- **THEN** no account is linked and no authenticated Generosity session is issued; a localized outcome leaves guest discovery available

### Requirement: Facebook Email Completion
Absent or unverified Facebook email SHALL NOT be fabricated or treated as verified. New users SHALL complete Generosity email verification unless a trusted verified-email claim is validated. Facebook account verification SHALL NOT substitute for email verification. An existing user SHALL be able to link Facebook through the explicit authenticated linking flow without requiring Facebook to disclose an email.

#### Scenario: Facebook supplies no verified email
- **WHEN** a new Facebook identity returns no email or an email without trusted verification
- **THEN** Generosity requests or confirms an address and verifies it before protected operations; an existing-address collision uses explicit linking rather than merging

#### Scenario: Existing account links without provider email
- **WHEN** a recently authenticated user with a verified Generosity email explicitly links a validated Facebook identity that returns no email
- **THEN** the identity is linked without replacing that account's email or granting additional privileges

### Requirement: First-Party Browser Sessions
The web client SHALL use a host-only Secure HttpOnly Laravel session cookie, an exact Generosity-origin allowlist and CSRF protection for writes. JavaScript SHALL NOT persist bearer tokens. Return destinations SHALL be fixed configuration, never arbitrary client input. Signing out SHALL revoke the current Generosity session without claiming to sign out of Google, Apple or Facebook.

#### Scenario: Separate institute application
- **WHEN** an unrelated institute site attempts credentialed access or supplies a redirect destination
- **THEN** it receives no authorized origin response or arbitrary redirect; institute-wide SSO is not provided

### Requirement: Native Authorization Handoff
Native clients SHALL use the system browser and exchange a one-use sixty-second handoff code bound to client, random state and PKCE S256. Only fixed Generosity callback schemes SHALL be accepted. Redirects SHALL NOT carry bearer or provider tokens. Native tokens SHALL expire after seven days and require sign-in again; silent renewal remains a separate lifecycle enhancement.

#### Scenario: Intercepted mobile callback
- **WHEN** another app intercepts a callback but lacks the initiating app's PKCE verifier
- **THEN** it cannot exchange the code; consumed, expired and wrong-client codes are also rejected

### Requirement: Secure Native Session Storage
Native sessions SHALL use iOS Keychain or Android Keystore-protected storage excluded from backup. Restoration SHALL validate the current account online before treating it as freshly authenticated. Failed logout SHALL report failure and offer retry without falsely claiming server revocation.

#### Scenario: Offline restoration
- **WHEN** a native client cannot validate a stored session online
- **THEN** anonymous browsing remains available and cached identity is not fresh authorization

#### Scenario: Logout cannot reach the backend
- **WHEN** session revocation fails
- **THEN** the client retains its credential for retry and reports that logout did not complete

### Requirement: Real Accounts Must Not Submit Demo Contributions
A validated remote account SHALL NOT create accepted-looking contributions in disconnected demonstration repositories. The UI SHALL identify its catalog/history/impact as disconnected until their respective API adapters are implemented.

#### Scenario: Disconnected contribution repositories
- **WHEN** a real account signs in while native repositories still contain demonstration data
- **THEN** demo writes are blocked and the UI does not present them as accepted server contributions

### Requirement: Feature-Specific Mobile Verification
Registration, sign-in and public discovery SHALL NOT require a mobile number. A server-owned policy SHALL name each action requiring a verified mobile number and its purpose before activation. The API SHALL enforce that requirement against the authenticated account's current verification state. Missing verification SHALL block only the specified action. No gated feature SHALL launch without its documented policy and available verification service.

#### Scenario: Required action without a verified number
- **WHEN** an account attempts an action whose approved policy requires mobile verification
- **THEN** the API refuses the operation with a stable localized-client error; the client explains the purpose, preserves the unsent draft within existing privacy rules and offers verification or cancellation

#### Scenario: Verification is unavailable or declined
- **WHEN** the user cancels verification or its delivery service is unavailable
- **THEN** the gated action stays unsent, unrelated features remain available and the client does not report success or submit the draft automatically later

#### Scenario: No phone for a person receiving help
- **WHEN** an assisted participant or referred beneficiary has no personal phone, app or account
- **THEN** existing assisted/referral routes remain available; any helper-account verification policy does not become a beneficiary eligibility requirement

### Requirement: Mobile Possession Proof
The backend SHALL verify possession using an expiring, one-use challenge bound to account, normalized number and purpose. A typed or provider-returned number SHALL start unverified. Resends SHALL invalidate earlier codes; per-account, number and network attempt/send limits SHALL precede delivery. Verification codes SHALL NOT enter logs or durable plaintext storage. Delivery confirmation SHALL NOT count as successful verification.

#### Scenario: Incorrect, expired or reused code
- **WHEN** a code is wrong, expired, superseded, already consumed or belongs to another account or number
- **THEN** verification fails without unlocking the action; configured retry limits prevent further attempts or sends when exhausted

#### Scenario: Successful verification
- **WHEN** the current account submits a valid challenge response
- **THEN** the backend atomically consumes the challenge and records the verified number and time; all clients read that server state without treating it as verified email, center authority or proof of a unique person

### Requirement: Mobile Number Changes and Identity Safety
Replacing or removing a verified number SHALL require recent reauthentication. Replacement SHALL verify the new number before switching atomically and invalidating the old verification and pending challenges. Removal SHALL clear verification and reapply affected action gates. A shared or reassigned number SHALL NOT merge accounts, transfer identities or authorize account recovery by itself.

#### Scenario: Number replacement fails
- **WHEN** a signed-in user cannot verify the proposed replacement
- **THEN** the current verified number remains unchanged and the proposed number grants no access

#### Scenario: Number already used elsewhere
- **WHEN** a number is shared with or reassigned from another account
- **THEN** proving possession grants no access to that account, its history or identities and reveals no account membership
