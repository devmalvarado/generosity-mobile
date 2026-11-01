## Purpose
Support monetary donations through an approved external provider with verifiable outcomes and currency-aware impact, without collecting payment credentials or assuming production readiness.

User decision, 2026-11-01: the institute would collect funds in its Mexican
BBVA account and then distribute them to centers. SAT donee authorization is
absent or pending, not verified as current. Collection/distribution eligibility,
provider and store approvals remain open; this is not direct settlement to each
center or authorization to start receiving money.

## ADDED Requirements

### Requirement: Explicit Provider and Recipient Gate
Money actions SHALL remain unavailable until an approved provider/environment, supported region/currency, eligible recipient mapping and operational support policy are configured. Recipient readiness SHALL come from a controlled operator/provider process, not visits, photos, ratings or client fields. Test mode SHALL be labeled and isolated from real balances. Spec completion SHALL not enable production charges.

#### Scenario: Provider or recipient is not ready
- **WHEN** a user selects money for a center without an eligible configured recipient
- **THEN** no checkout session or success record is created and time/goods help remains available

### Requirement: Reviewed Donation Intent
Authenticated donors SHALL review the legal collecting entity, intended beneficiary center/program, purpose, amount, currency and any known fees before explicitly continuing. The backend SHALL validate positive bounded minor-unit amounts, currency, current need eligibility and the controlled recipient. Client-supplied recipient accounts or changed amounts SHALL not redirect funds. An intent SHALL retain its donor, collecting entity, center/need snapshot and provider correlation.

#### Scenario: Altered recipient or unsupported amount
- **WHEN** a modified client submits another recipient account, unsupported currency or invalid precision/amount
- **THEN** the backend rejects the request without creating a charge or rewriting the intended beneficiary

### Requirement: Hosted Payment Boundary
The backend SHALL create an idempotent provider-hosted checkout session for the accepted intent. Native/public app surfaces SHALL not collect card, security-code or bank-account details. Allowed checkout/return hosts SHALL be configured, and returns SHALL be bound to the original intent. Closing or returning from checkout SHALL show the known processing/cancelled state, never prove payment by itself.

#### Scenario: Forged success return
- **WHEN** a user returns with a success-looking URL but no verified provider result exists
- **THEN** history remains pending/unknown and monetary impact does not increase

### Requirement: Verified Idempotent Outcomes
Payment status SHALL be reconciled from authenticated provider events and server-side provider verification matching intent, recipient, amount and currency. Duplicate, late or out-of-order events SHALL not double-count or regress confirmed state. Invalid events SHALL be rejected; lost events SHALL be recoverable through reconciliation. Intent/session expiry SHALL not erase an actual late payment result.

#### Scenario: Duplicate success event after restart
- **WHEN** the same verified provider success is processed again after a service restart
- **THEN** one payment ledger result and one monetary history contribution remain, with no second charge or duplicate total

#### Scenario: Success arrives after checkout closed
- **WHEN** a genuine provider success arrives after the client marked checkout closed or the need later closed
- **THEN** the original intent is reconciled without redirecting funds, losing the payment or reopening a goods need

### Requirement: Separate Financial Lifecycle
Monetary records SHALL use provider-confirmed pending/paid/failed/cancelled/refunded/disputed states separately from staff-confirmed time/goods fulfillment. Staff SHALL not mark a payment paid. Partial/full refunds and disputes SHALL preserve the original ledger and update authoritative net impact once. Donors SHALL have a support/refund route; a refund request SHALL not be presented as an approved refund.

#### Scenario: Partial refund or dispute
- **WHEN** the provider confirms a partial refund or an unresolved dispute
- **THEN** history shows the adjustment/status and eligible net monetary impact excludes refunded/disputed amounts without altering goods/time records

### Requirement: Currency-Aware Private History
History and Impact SHALL display verified gross amounts, available fees, adjustments and net donated totals by currency. Unknown fees or settlement SHALL be labeled, and paid SHALL not imply recipient payout. Different currencies SHALL not be silently summed or converted. Provider receipts SHALL be distinguished from tax receipts. Payments alone SHALL not grant center-validated review or gamification eligibility.

#### Scenario: Two currencies in history
- **WHEN** a donor has verified payments in two supported currencies
- **THEN** Impact shows separate labeled currency totals and no invented combined balance or exchange rate

### Requirement: Operational Release Safety
Production enablement SHALL require recorded provider/recipient eligibility, operating regions/currencies, applicable compliance/store review, refund/dispute ownership and tested reconciliation. Secrets SHALL remain server-side and diagnostics SHALL omit sensitive payment data. A disable switch SHALL block new intents while preserving incoming-event reconciliation and history. Sandbox acceptance SHALL cover failure, cancellation, duplicates and refunds.

#### Scenario: Disable checkout during an incident
- **WHEN** operations disables new monetary donations
- **THEN** no new intents are accepted while existing provider results, adjustments and owned history continue to reconcile

### Requirement: Institute Collection and Controlled Distribution
Institute collection SHALL require legal/accounting review, beneficiary agreements, verified bank ownership and provider acceptance of Mexican onward distribution. Intent records SHALL separate collector and beneficiary. Allocation, collection, fees, settlement, transfer and center receipt SHALL have linked private evidence with scoped permissions and audited corrections. Bank-operator distribution MAY be reconciled without a bank API, consumer wallet or automated transfers.

#### Scenario: Bank account exists without provider acceptance
- **WHEN** an operator supplies the institute's BBVA account but the fundraising model has not passed its eligibility gates
- **THEN** no checkout is enabled merely because banking details or a charity label exist

#### Scenario: Institute receives a payment before the center
- **WHEN** provider evidence confirms collection and settlement to the institute but no verified onward delivery exists
- **THEN** history distinguishes that state from delivery to the center, with no completed-help award or unsupported center-received claim

#### Scenario: Request changes a distribution destination
- **WHEN** an unauthorized client or operator changes a center or bank destination
- **THEN** the request is rejected without moving money or rewriting the original allocation; approved changes require audited authorization and the donor-facing purpose rules

### Requirement: Failed Allocation and Donor Rights
Campaigns SHALL disclose delivery, costs, support and failure-resolution rules before collection. Purpose changes SHALL require an applicable accepted rule or fresh donor authorization. Refund/dispute support SHALL remain ownership-verifiable after deletion without restoring access. Financial retention SHALL follow the deletion policy. Ordinary receipts SHALL NOT claim deductibility without transaction-specific issuer eligibility.

#### Scenario: Donor reviews campaign conditions
- **WHEN** a donor prepares to confirm a payment
- **THEN** delivery timeframes, administrative/provider costs, responsible support and rules for cancellation, unmet targets, unavailable recipients and unallocated funds are available before authorization

#### Scenario: Center cannot receive the collected funds
- **WHEN** an intended center becomes ineligible after collection
- **THEN** operations follows the disclosed resolution/refund rule, records the result and does not report delivery or silently fund another center

### Requirement: Platform-Specific Fundraising Eligibility
Each platform/territory SHALL have approved fundraising and recipient eligibility before exposing payments or links. Review SHALL cover the actual institute/beneficiary model, distinguishing provider, SAT and store determinations. Apple in-app and outside-app routes SHALL be reviewed separately; Android eligibility SHALL NOT be inferred from iOS or association status. Commercial/digital purchases SHALL require separate policy review, never a charity exemption.

#### Scenario: Configure store donation treatment
- **WHEN** operations reviews the intended iOS and Android fundraising routes
- **THEN** it records applicable Apple nonprofit/Apple Pay conditions and Google tax-exempt donation eligibility separately, with evidence for the actual collector and beneficiaries

#### Scenario: Institute or channel approval is unresolved
- **WHEN** a provider, store route or applicable recipient approval is missing, including the current institute setup
- **THEN** the affected monetary action and payment links stay disabled, other helping methods remain available and a sandbox result cannot enable production
