## Purpose

Classify centers with stable localized category codes and keep classification distinct from capacity, medicine authorization, and official verification.

## ADDED Requirements

### Requirement: Stable multi-category classification
The catalog SHALL support approved stable codes with Spanish/English labels for the requested foster-home, nursing-home, disability, addictions, health, community-kitchen, blood-donation, migrant-shelter, homeless-shelter, medical-dispensary and training/education categories. Existing categories SHALL remain readable through a versioned migration. Multiple categories SHALL be allowed; category membership SHALL NOT certify service capacity, medicine authorization or official verification.

#### Scenario: Blood donation is distinct
- **WHEN** a center is classified as blood donation
- **THEN** discovery shows that localized category without relabeling it as organ/tissue donation or assuming that it accepts donors at all hours

#### Scenario: Blood filter uses explicit source classification
- **WHEN** a user selects `blood_donation` (Donación de sangre / Blood donation)
- **THEN** map and list return only explicitly classified blood-service centers, while the broad `health` category can still include them
- **AND** organ/tissue donation remains a separate category and generic health centers are not inferred to accept blood

#### Scenario: Old native readers refresh after classification correction
- **WHEN** an old client requests the mobile-v1 snapshot without `category_version=2`
- **THEN** the API projects `blood_donation` to the known `health` code without changing stored classifications or the JSON schema version
- **AND** updated clients explicitly request `category_version=2` and preserve the specific category through refresh and offline cache

#### Scenario: Correcting the reviewed blood-service batch
- **WHEN** the targeted migration adds blood donation to the 39 reviewed imported blood-service IDs
- **THEN** existing health membership, identity, contacts, coordinates, source links, needs and staff memberships are retained, category changes are audited, and changed center versions advance
- **AND** rerunning the correction does not increment unchanged records, unknown/unreviewed centers are not reclassified, and rollback removes only the added category

### Requirement: Explicit migration and protected locations
Category changes SHALL retain source provenance and an explicit migration map. Sensitive shelter locations SHALL use approved public contact/referral locations in public discovery; category filtering SHALL NOT expose private addresses.

#### Scenario: An old imported category has no approved mapping
- **WHEN** an import contains an unmapped category code
- **THEN** validation reports it without silently reclassifying the center or overwriting the last accepted catalog
