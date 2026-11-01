## Purpose
Define real account entry and session lifecycle across both apps and the backend while preserving anonymous discovery and preventing fabricated identities.

The configured first-party provider for the PHP implementation is Laravel
framework authentication with server-only password hashes, verified email and
Sanctum bearer tokens for native apps and CSRF-protected sessions for the web.
Google and Apple authorization-code identity entry is layered onto the same
account store. Live provider credentials are pending; see the implementation audit.
Facebook entry and feature-specific mobile-number verification below are planned,
not implemented. A verified social identity, email and mobile number are separate proofs.

The 2026-11-01 deletion requirements below are also planned, not deployed. The
user requires permanent loss of access to a deleted account; retaining a complete
soft-deleted personal profile does not satisfy the data-removal requirements.

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

### Requirement: Verified contact for group and event creation
The backend SHALL require a currently verified private mobile number before group/event creation, in addition to earned organizer eligibility and action permissions. Phone verification SHALL NOT grant center roles or bypass points/level requirements. Removal of phone proof SHALL block subsequent creation. Clients SHALL preserve drafts and require explicit resubmission after verification.

#### Scenario: Direct API creation without verified contact
- **WHEN** an otherwise eligible account without current phone verification calls a group/event creation endpoint
- **THEN** the backend rejects it with a stable phone-verification-required error, creates nothing, and the client preserves the draft without automatic resubmission

#### Scenario: Verification does not grant organizer eligibility
- **WHEN** a phone-verified account lacks the required prior help, points or permissions
- **THEN** event/group creation remains denied under the existing organizer policy

#### Scenario: Unrelated participation remains available
- **WHEN** a person without verified mobile contact browses, enters an account, offers ordinary help or seeks beneficiary assistance
- **THEN** this creation policy adds no phone requirement to those operations

### Requirement: Accessible Confirmed Account Deletion
Account settings on iOS, Android and web SHALL expose account deletion. A public mobile-first web resource SHALL accept ownership-verified requests without an installed app. Recent reauthentication and informed confirmation SHALL be required. The normal flow SHALL NOT depend on support or organizational ownership transfer. Responses/status access SHALL preserve privacy, and offline requests SHALL NOT claim server acceptance.

#### Scenario: Confirm irreversible deletion
- **WHEN** an owner reaches the confirmation step
- **THEN** the flow explains permanent access loss, completion time, pending commitments and retained data before an explicit decision

#### Scenario: User no longer has the app
- **WHEN** the owner opens the published deletion resource and completes ownership verification and explicit confirmation
- **THEN** the backend accepts the same deletion request as the native flow and provides a private reference/status route without requiring app installation

#### Scenario: Deletion submission has no acknowledged result
- **WHEN** a request is offline or its response is lost
- **THEN** the client shows an unconfirmed outcome and offers reconciliation; retries do not create a second request or reactivate an accepted deletion

### Requirement: Irreversible Access Closure
Acceptance SHALL atomically block authorization and revoke sessions, tokens, handoffs, recovery/verification challenges and role access. All authentication and protected read/write paths SHALL enforce deletion, including during cleanup. Recovery, callbacks, queued work and backups SHALL NOT restore the account. Provider credentials SHALL be revoked with retries, without deleting external provider accounts or restoring Generosity access.

#### Scenario: Existing session or recovery link after deletion
- **WHEN** any client uses an old session, bearer token, reset link, verification link or handoff after deletion acceptance
- **THEN** protected access and mutations are denied and no active account/session is recreated

#### Scenario: Social sign-in or fresh registration later
- **WHEN** the former owner returns through a provider callback or attempts registration
- **THEN** the deleted account cannot be accessed or silently restored; any permitted new account requires explicit separate registration with a new identifier and no inherited history, roles or points

#### Scenario: Provider revocation temporarily fails
- **WHEN** a provider cannot confirm token revocation after Generosity accepts deletion
- **THEN** its cleanup retries while all Generosity access stays blocked, and no claim is made that the external provider account itself was deleted

#### Scenario: Mutation races with deletion
- **WHEN** an earlier authenticated mutation reaches its commit boundary after deletion acceptance
- **THEN** the authorization block prevents a new account-owned mutation from committing or restoring private role access

### Requirement: Purpose-Bound Data Removal
Deletion SHALL remove or irreversibly anonymize associated personal data; soft deletion SHALL only control access/state. Before release, an approved retention matrix SHALL define retained categories, purpose/basis, scope, expiry, backup handling and provider deletion. Retained evidence SHALL be minimal, restricted and expire without marketing or profile reconstruction. Shared commitments SHALL be reassigned/minimized without indefinitely blocking deletion.

#### Scenario: Financial evidence must be retained
- **WHEN** a reviewed retention rule requires a limited transaction record after account deletion
- **THEN** only the necessary fields remain under restricted access for the documented period, the owner receives an explanation, and other personal data is removed on schedule

#### Scenario: Only a deletion marker is necessary
- **WHEN** the retention policy requires evidence preventing resurrection of an old account identifier
- **THEN** the marker contains no complete profile or reusable credential, public identity and unnecessary provider links are removed, and the marker follows its documented purpose and retention rule

#### Scenario: Purge job fails or a backup is restored
- **WHEN** deletion cleanup fails or a backup predating deletion is recovered
- **THEN** access stays blocked, cleanup resumes idempotently and deletion records are reapplied before restored services accept requests

### Requirement: Cross-Client Deletion Completion
Clients SHALL clear private credentials, caches, drafts and outbox work upon learning of acknowledged deletion, including on foreground/reconnection. Disconnected devices SHALL NOT be claimed erased. The server SHALL immediately stop account notifications and queued writes. Deletion receipts/status SHALL use a limited channel without restored access; completion SHALL disclose retained records/backup expiry and unresolved cleanup.

#### Scenario: Another device reconnects with pending work
- **WHEN** a device with stale private cache and queued mutations reconnects after deletion
- **THEN** it clears that account's private state, submits no accepted mutations and returns to anonymous discovery without touching a different account

#### Scenario: Required purge or provider cleanup is unresolved
- **WHEN** the owner checks the private deletion status
- **THEN** access closure is distinguished from incomplete cleanup, completion is not falsely claimed and justified retention/backup expiry is explained

### Requirement: Published Versioned Terms and Separate Privacy Choices
Password and social registration SHALL present approved localized terms and a separate privacy notice before account creation, recording accepted version/time. Optional consent and device permissions SHALL remain separate. Drafts, placeholders and unimplemented promises SHALL block legal-page release acceptance. Material updates SHALL follow the approved notice/acceptance policy without retroactively changing donation purpose.

#### Scenario: Social registration bypasses the password form
- **WHEN** a new social identity completes provider authorization without accepting the currently required terms
- **THEN** Generosity presents the same legal entry step before creating its account, without treating provider consent as acceptance of Generosity's terms


### Requirement: Adult account eligibility and current legal acceptance
The system SHALL require new accounts to declare age 18+ and legal adulthood in their residence. Password and enabled social registration SHALL require current terms acceptance and privacy acknowledgement, with server-stamped version/date and no birth-date collection. Public ES/EN documents SHALL identify the institute and its working staff-operated contact; optional consent SHALL remain separate.

#### Scenario: Required declaration is absent or declined
- **WHEN** a registration omits or declines the adult declaration or a required legal acknowledgement, or submits an obsolete document version
- **THEN** the server rejects it without creating an account or sending account email
- **AND** hosted registration exposes localized errors and working links to both documents

#### Scenario: Existing accounts precede policy receipts
- **WHEN** the additive migration reaches an existing account
- **THEN** receipt fields remain unset rather than inventing retrospective consent
- **AND** a future material policy change requires a separate acceptance rollout

### Requirement: Scoped operator-assisted permanent access block
The system SHALL provide an operator-confirmed, audited permanent access block that excludes closed accounts from authentication/mutations, revokes credentials and pending grants, and suppresses queued account email. Public wording SHALL distinguish access closure from data erasure and provide a working rights-request channel. Store deletion completion SHALL remain separate.

#### Scenario: Closed account tries another access route
- **WHEN** a closed account retries its password, token, reset/verification link, native handoff, social subject or registration email
- **THEN** access remains blocked and no replacement account is silently created
- **AND** repeat operator execution does not duplicate the closure audit

#### Scenario: Cancellation needs retained-record review
- **WHEN** a person requests cancellation of data as well as access closure
- **THEN** institute staff handle cancellation separately under the published notice and document minimum retention grounds and expiry criteria
- **AND** full purge, provider revocation and native self-service deletion remain explicit follow-up work
