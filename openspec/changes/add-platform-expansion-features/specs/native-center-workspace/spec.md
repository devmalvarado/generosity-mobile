## Purpose
Let assigned center staff manage supported center work from native apps through the same server-authorized contracts as the portal, without granting consumer or organizer accounts staff powers.

## ADDED Requirements

### Requirement: Explicit Staff Mode
Android and iOS SHALL expose a separate staff workspace only for current server-assigned center roles, with an explicit active-center selector. Public consumer discovery SHALL remain read-only. Organizer eligibility, pending onboarding, installed app state or cached roles SHALL not authorize staff actions. Switching centers/accounts SHALL isolate drafts and private data.

#### Scenario: Organizer without a center role opens staff mode
- **WHEN** that user attempts the staff route
- **THEN** access is denied without changing their community-organizing eligibility

### Requirement: Portal Contract Parity
Authorized staff SHALL manage supported needs, profile/hours/media, event occurrences and contribution confirmation/fulfillment using the same validation, version checks and audit records as the portal. The workspace SHALL show only role-permitted actions. Independent validation rules SHALL still prevent self-validation; native clients SHALL not set verification, point balances or provider-paid status.

#### Scenario: Web staff edit races a native edit
- **WHEN** native staff submits an older expected version
- **THEN** the conflict preserves the newer server record and offers an explicit refresh/reapply flow without silent overwrite

### Requirement: Honest Pending Work and Revocation
Native forms SHALL distinguish local drafts/pending requests from accepted publication or validation. Protected writes SHALL reauthorize online; stale cached permissions SHALL not succeed offline. Revocation SHALL stop queued mutations and clear private staff views while preserving permitted recovery of the user's own draft according to retention policy.

#### Scenario: Membership is revoked before reconnect
- **WHEN** a queued publication reaches the server
- **THEN** it is rejected and never appears in the public catalog despite a previously enabled native button

### Requirement: Accessible Limited Native Scope
Both native workspaces SHALL support Spanish/English, enlarged text, safe media selection, clear errors and usable phone navigation. Complex onboarding, verification, fiscal and operations reviews SHALL link to authenticated mobile-first web routes where not implemented natively. Such links SHALL not carry credentials or silently grant another role; unavailable workflows SHALL be labeled honestly.

#### Scenario: Staff opens verification review from iOS
- **WHEN** the permitted workflow is web-only
- **THEN** an approved authenticated browser route opens with the intended center context but no token-bearing public URL or bypassed review authorization
