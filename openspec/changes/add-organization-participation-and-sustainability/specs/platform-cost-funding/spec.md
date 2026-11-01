## Purpose
Cover Generosity operating costs through clearly identified commercial revenue without mixing platform charges, beneficiary donations or helping recognition.

## ADDED Requirements

### Requirement: Configured Commercial Offers
The backend SHALL keep commercial checkout unavailable until seller, product, price, currency, term, renewal/cancellation/refund terms and provider are configured. An organization payer SHALL explicitly review and accept the offer through an authorized representative. Browsing and ordinary helping SHALL remain available without buying a commercial product.

#### Scenario: Price or seller is missing
- **WHEN** a business requests a paid listing without a complete active offer
- **THEN** checkout stays unavailable and neither a guessed fee nor an active placement is created

### Requirement: Separate Hosted Billing
Commercial charges SHALL use a provider-hosted or platform-approved checkout and a ledger distinct from donations, event tickets and awards. Clients SHALL not collect card/bank credentials or choose a different recipient. Activation SHALL require verified provider evidence matching the order, amount and currency. Each platform SHALL satisfy its billing/release review before enabling its checkout entry.

#### Scenario: Buyer returns from checkout with a success parameter
- **WHEN** no matching verified payment outcome exists
- **THEN** the order remains pending and no advertising or listing entitlement activates

### Requirement: Bounded Entitlement Lifecycle
Orders SHALL distinguish pending, paid, failed, cancelled, refunded and disputed outcomes from content approval and service dates. Duplicate/out-of-order provider events SHALL reconcile once. Renewals SHALL require explicit consent and display the next charge/term. Cancellation SHALL stop future renewal under disclosed terms while preserving receipts and access through any paid remaining term.

#### Scenario: Renewal fails
- **WHEN** the provider reports failure and the paid term expires
- **THEN** commercial visibility stops under the published terms without removing the organization's helping history or members' points

#### Scenario: Duplicate refund arrives
- **WHEN** the same verified refund is processed twice
- **THEN** net revenue and applicable entitlement change once, with the original charge retained

### Requirement: Honest Cost Coverage
Authorized operations SHALL record dated categorized costs with currency, source and planned/incurred/paid state. Reports SHALL separate invoiced revenue, collected funds, fees, refunds and net receipts by currency and period. Coverage SHALL use a disclosed basis and identify missing costs; unknown amounts SHALL not appear as zero or support a claim that all operating costs are covered.

#### Scenario: Hosting is known but SMS costs are missing
- **WHEN** operations opens the monthly coverage report
- **THEN** known costs and net commercial receipts are shown with an incomplete-cost warning, not a fabricated surplus

### Requirement: Financial Purpose and Access
Platform sponsorship/listing receipts SHALL identify their commercial purpose and SHALL not claim beneficiary delivery, donation tax deductibility or helping points. Organization owners SHALL access only their own billing history; detailed platform finances SHALL require a scoped operations role. Public cost reports SHALL expose only approved aggregates. Enabling platform revenue SHALL not divert existing charitable donations.

#### Scenario: Company purchases an annual listing
- **WHEN** payment is reconciled
- **THEN** its commercial history updates while donation totals, personal points and center verification remain unchanged

### Requirement: Accessible Recovery and Disablement
Billing review, history, cancellation and support SHALL work in Spanish/English on mobile-first web and permitted native entry points. Pending or failed checkout SHALL preserve the order reference and require explicit retry. A disable control SHALL stop new orders while preserving reconciliation, refunds, owned history and support for existing ones.

#### Scenario: Billing is paused during an incident
- **WHEN** the commercial feature is disabled with unsettled orders
- **THEN** no new checkout begins and prior provider outcomes continue to reconcile safely
