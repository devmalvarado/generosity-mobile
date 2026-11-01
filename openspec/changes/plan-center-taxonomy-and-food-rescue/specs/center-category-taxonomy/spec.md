## Purpose

Classify centers with stable localized category codes and keep classification distinct from capacity, medicine authorization, and official verification.

## ADDED Requirements

### Requirement: Stable multi-category classification
The catalog SHALL support approved stable codes with Spanish/English labels for the requested foster-home, nursing-home, disability, addictions, health, community-kitchen, blood-donation, migrant-shelter, homeless-shelter, medical-dispensary and training/education categories. Existing categories SHALL remain readable through a versioned migration. Multiple categories SHALL be allowed; category membership SHALL NOT certify service capacity, medicine authorization or official verification.

#### Scenario: Blood donation is distinct
- **WHEN** a center is classified as blood donation
- **THEN** discovery shows that localized category without relabeling it as organ/tissue donation or assuming that it accepts donors at all hours

### Requirement: Explicit migration and protected locations
Category changes SHALL retain source provenance and an explicit migration map. Sensitive shelter locations SHALL use approved public contact/referral locations in public discovery; category filtering SHALL NOT expose private addresses.

#### Scenario: An old imported category has no approved mapping
- **WHEN** an import contains an unmapped category code
- **THEN** validation reports it without silently reclassifying the center or overwriting the last accepted catalog
