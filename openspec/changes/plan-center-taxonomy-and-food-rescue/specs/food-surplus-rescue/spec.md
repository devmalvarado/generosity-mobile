## Purpose

Match time-bounded surplus food to participating centers under an approved handling protocol, without treating an offer as received help or a safety certification.

## ADDED Requirements

### Requirement: Authorized time-bounded surplus offers
Authorized participating producers, schools or organizations SHALL publish surplus only under an approved local operating protocol, with quantity/unit, source, food description, preparation/handling declarations, allergens when known, pickup window and expiry. Missing required protocol data SHALL block publication; the app SHALL NOT certify food as safe from a category or producer claim alone.

#### Scenario: A culinary school offers surplus
- **WHEN** an authorized school operator submits the required declarations and pickup deadline
- **THEN** eligible participating centers can review the offer, while consumers see only approved public availability without needing to install the app

### Requirement: Accepted quantities and actual receipt
Center acceptance and reservations SHALL enforce compatible food/service rules and atomic available quantities. Pickup claims, actual receipt, rejection, cancellation and expiry SHALL be separate versioned states. Idempotent retries SHALL NOT duplicate quantities. Expired offers SHALL be unavailable, and offered/reserved amounts SHALL NOT count as received or verified impact.

#### Scenario: Two centers request the last portions
- **WHEN** concurrent reservations exceed the remaining quantity
- **THEN** only valid quantities are accepted and the unsuccessful request receives the latest availability without a duplicate reservation

### Requirement: Private recipient access and accountable release
Public map/list and sharing SHALL expose approved participating locations, service windows and availability without beneficiary identities or private addresses. Receiving assistance SHALL NOT require an app, account or personal phone. Operational food-handling acceptance SHALL precede activation and be recorded separately from software tests.

#### Scenario: A person without a phone needs a meal
- **WHEN** a helper searches for currently available food support
- **THEN** the helper can provide the approved service location and instructions without registering the recipient or publishing personal information
