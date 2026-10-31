## Purpose
Replace hard-coded production centers with validated catalog data that can be loaded from a file, published by the backend and safely refreshed on mobile devices.

## ADDED Requirements

### Requirement: Validated Catalog Import
Catalog imports SHALL follow the documented versioned schema with stable center/need identifiers, localized fields, supported codes, valid coordinates and valid quantity/date fields. Invalid imports SHALL fail atomically with a diagnostic rather than partially replacing the active catalog. Repeating an import SHALL not duplicate records.

#### Scenario: Load a valid JSON file
- **WHEN** a catalog file passes schema and relationship validation
- **THEN** its centers and needs become available through the repository used by both apps

#### Scenario: Invalid or conflicting identifiers
- **WHEN** a file contains duplicate IDs, an unknown required code or a need linked to a missing center
- **THEN** the import is rejected and the previously published catalog stays intact

### Requirement: Bundled Fallback and Backend Refresh
Both apps SHALL support a validated bundled catalog for initial discovery and a versioned backend source for refresh. Successful responses SHALL replace public cache atomically; failed refreshes SHALL retain the last known good data. Demo fixtures SHALL be labeled as samples and SHALL NOT be presented as verified real centers.

#### Scenario: First launch without network
- **WHEN** no server cache exists but a valid bundled catalog is available
- **THEN** users can browse it offline and see its source/update status

#### Scenario: Network or response failure
- **WHEN** a refresh times out or fails validation
- **THEN** the previous catalog remains usable with a localized stale/error indicator and retry action

#### Scenario: No usable catalog
- **WHEN** neither a valid bundle nor cached/server data is available
- **THEN** discovery shows a non-crashing empty/error state rather than invented centers

### Requirement: Consistent Refresh and Removal
Refresh SHALL preserve stable IDs, synchronize map/list filters and apply removed centers or closed needs consistently. Historical contributions SHALL keep their recorded center/need labels when catalog items disappear. Removed items SHALL no longer accept new offers.

#### Scenario: A selected need is removed
- **WHEN** a newer catalog omits the currently selected need
- **THEN** the app explains that it is unavailable, disables new offers and retains existing historical records

### Requirement: Catalog Ownership and Capacity Preservation
Only authorized operators SHALL perform catalog imports; assigned center staff SHALL publish need changes through the center-scoped API. Both paths SHALL produce authoritative catalog revisions, preserve the contribution ledger and SHALL NOT reset commitments or erase history. Updates that conflict with existing capacity SHALL be rejected for correction.

#### Scenario: Import lowers capacity below commitments
- **WHEN** a new required goods quantity is smaller than accepted commitments
- **THEN** publication is rejected without overwriting the active capacity or contributions

### Requirement: Imported and Center-Managed Reconciliation
Imports SHALL track source ownership and preserve center-managed needs omitted from an imported file. An import SHALL NOT silently overwrite a newer staff edit or publish a private draft. Conflicting updates to the same ID SHALL require explicit, authorized reconciliation against the current version. IDs and historical references SHALL remain stable across sources.

#### Scenario: Import omits a portal-created need
- **WHEN** an operator imports a valid file that does not contain a center-managed need
- **THEN** that need and its contribution ledger remain intact rather than being treated as removed

#### Scenario: Import conflicts with a staff edit
- **WHEN** a file contains an older version of a need updated by center staff
- **THEN** the import reports a conflict without partially replacing the active catalog or losing the staff update

### Requirement: Discoverable Data Freshness
Catalog records SHALL expose source revision and update timestamps. Both apps SHALL show offline/stale state without treating imported status, a recent timestamp or a photo as official center verification.

#### Scenario: Inspect cached center details
- **WHEN** the user views a cached center while refresh is unavailable
- **THEN** the app distinguishes cached data from a recent successful refresh and preserves the imported verification label
