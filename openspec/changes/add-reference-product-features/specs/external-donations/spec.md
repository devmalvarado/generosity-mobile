## Purpose
Support monetary donations through an approved external provider with verifiable outcomes and currency-aware impact, without collecting payment credentials or assuming production readiness.

## ADDED Requirements

### Requirement: Explicit Provider and Recipient Gate
Money actions SHALL remain unavailable until an approved provider/environment, supported region/currency, eligible recipient mapping and operational support policy are configured. Recipient readiness SHALL come from a controlled operator/provider process, not visits, photos, ratings or client fields. Test mode SHALL be labeled and isolated from real balances. Spec completion SHALL not enable production charges.

#### Scenario: Provider or recipient is not ready
- **WHEN** a user selects money for a center without an eligible configured recipient
- **THEN** no checkout session or success record is created and time/goods help remains available

### Requirement: Reviewed Donation Intent
Authenticated donors SHALL review recipient identity, purpose, amount, currency and any known fees before explicitly continuing. The backend SHALL validate positive bounded minor-unit amounts, currency, current need eligibility and the controlled recipient. Client-supplied recipient accounts or changed amounts SHALL not redirect funds. An intent SHALL retain its donor, center/need snapshot and provider correlation.

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
