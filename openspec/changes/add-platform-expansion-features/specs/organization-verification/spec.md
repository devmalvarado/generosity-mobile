## Purpose
Represent reviewed organization evidence with explicit issuer, scope and validity rather than conflating visits, staff access or platform review with official external accreditation.

## ADDED Requirements

### Requirement: Scoped Verification Claims
Each verification claim SHALL identify its issuer, criteria/version, scope, evidence reference, decision date and validity period. A Generosity review SHALL be labeled as such, not government certification. External official status SHALL require attributable evidence from the named authority. Visits, points, ratings, photos, memberships and payments SHALL never create a verified claim automatically.

#### Scenario: Imported record says officially verified without provenance
- **WHEN** its issuer and evidence cannot be established
- **THEN** the public interface shows an unconfirmed legacy status rather than an active official badge

### Requirement: Independent Evidence Decisions
Assigned reviewers SHALL inspect only required protected evidence, record requests/decisions and avoid reviewing their own or represented organization. High-risk approval SHALL follow the configured independent-review policy. Files SHALL have validated types/limits, safe private access, retention and access audits; document upload alone SHALL not establish authenticity.

#### Scenario: Reviewer is also center staff
- **WHEN** that reviewer attempts to approve their center's verification
- **THEN** the decision is denied and routed to an independent authorized reviewer

### Requirement: Expiry Revocation and Appeals
Claims SHALL transition through pending, active, rejected, expired, suspended or revoked with dated reasons and appeal records. Expiry/revocation SHALL remove active public claims and reconcile dependent gates without erasing contribution history. An appeal SHALL not restore status until approved. Providers SHALL independently control payout/fiscal eligibility.

#### Scenario: Verification expires during a cached session
- **WHEN** the validity end is reached
- **THEN** current public views no longer present active verification and protected actions consult the current server state

### Requirement: Understandable Public Status
Native and web center details SHALL display only approved public claim metadata, with localized issuer, scope, checked date and limitations. Private documents and reviewer identities SHALL not be exposed. Different claim types SHALL not collapse into a single badge that implies service quality, tax deductibility or guaranteed safety.

#### Scenario: Center is reviewed but not a tax-qualified issuer
- **WHEN** a donor opens its verification details
- **THEN** the platform-review scope is visible and no tax receipt or deductibility promise is inferred
