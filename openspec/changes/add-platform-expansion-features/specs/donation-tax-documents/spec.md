## Purpose
Support jurisdiction-specific donation tax documents through eligible issuers and approved providers while keeping ordinary payment receipts distinct and avoiding unsupported deductibility claims.

## ADDED Requirements

### Requirement: Jurisdiction and Issuer Gate
Tax-document requests/issuance SHALL require an approved country/jurisdiction policy, eligible issuer, provider/integration and effective rule version for the relevant transaction. Interface language, nationality or a Generosity badge SHALL not establish eligibility. Missing configuration SHALL show unavailable without fabricating a fiscal PDF, identifier or deductible status. Ordinary payment receipts SHALL remain separately labeled.

#### Scenario: Country is not configured
- **WHEN** a donor requests a tax document for an unsupported issuer/jurisdiction
- **THEN** issuance is unavailable and any payment receipt is not relabeled as tax-deductible evidence

### Requirement: Private Necessary Fiscal Data
Only fields required by the selected reviewed policy SHALL be requested with purpose, recipients and retention disclosed. Donors SHALL review corrections before submission; tax identifiers and documents SHALL remain private and excluded from public profiles, analytics and notification previews. Issuer/provider secrets and signing keys SHALL never be exposed to clients.

#### Scenario: Change display language
- **WHEN** a donor switches between Spanish and English
- **THEN** the selected jurisdiction and eligibility do not change and no new fiscal fields are inferred from language

### Requirement: Evidence-Based Issuance
Issuance SHALL bind to an authoritative donation, eligible issuer and policy-required completion/receipt evidence. Offers, unverified payment returns, event tickets and volunteer hours SHALL not be assumed deductible donations. Goods SHALL require supported issuer-reviewed descriptions/valuation rules; the app SHALL not invent value. Provider/issuer acknowledgment, not a client request, SHALL establish issuance.

#### Scenario: Request a document for an unpaid pledge
- **WHEN** the evidence does not meet the configured issue conditions
- **THEN** the request remains pending or is rejected with a reason and no issued document is claimed

### Requirement: Idempotent Document Lifecycle
Requests SHALL retain issuer/provider correlation and distinguish requested, processing, issued, failed, cancelled and replaced states as supported by the policy. Retries SHALL not issue duplicates. Corrections, refunds and disputes SHALL trigger the applicable reviewed adjustment process, preserve linked document history and await authoritative outcomes. Financial refund and fiscal cancellation SHALL not be treated as the same transaction.

#### Scenario: Provider response is lost
- **WHEN** the client retries a request that the provider already issued
- **THEN** reconciliation recovers the same issued document instead of creating a second fiscal record

### Requirement: Owned Retrieval and Authenticity
Authorized donors/issuer staff SHALL retrieve issued documents in required formats, including an accessible representation where supported. Public knowledge of an ID SHALL not expose a fiscal document. Validity and replacement status SHALL come from the approved issuer/provider verification path; the platform SHALL not guarantee a person's tax treatment or present recognition certificates as fiscal evidence.

#### Scenario: Receipt is later replaced
- **WHEN** its owner opens the old document entry
- **THEN** the replacement/cancellation status and permitted successor are shown without silently editing the original artifact

### Requirement: Reviewed Release and Operations
Each enabled jurisdiction SHALL have dated fiscal review, provider sandbox evidence, issuer responsibilities, retention and support ownership. New issuance SHALL be disableable without blocking existing retrieval/reconciliation. Production emission SHALL require separate approval; test documents SHALL be unmistakably non-fiscal and isolated from live ledgers.

#### Scenario: Jurisdiction rules change
- **WHEN** an enabled configuration becomes unsupported or expires
- **THEN** new issuance stops until reviewed configuration is active while existing records remain privately accessible and traceable
