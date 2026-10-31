## MODIFIED Requirements

### Requirement: List-Based Discovery
The mobile app SHALL provide a list view using the same public search and filter criteria as the map. Matching records with public coordinates SHALL appear consistently in both. Contact-only records without public coordinates SHALL remain list-discoverable where public criteria can be evaluated, labeled location unavailable. Neither view SHALL fabricate a position or use protected coordinates to derive search results, proximity or ordering.

#### Scenario: User switches from map to list
- **WHEN** the user opens the list view after applying filters
- **THEN** the list includes the same matching publicly locatable Kindness Centers represented on the map, plus clearly identified contact-only matches supported by public criteria

#### Scenario: Center has no public coordinates
- **WHEN** an anonymous user searches a published contact-only center by its public name or category
- **THEN** the list offers its public details/contact without a map marker, exact distance or route, and a radius-only search does not infer a match from private coordinates

### Requirement: Center Detail View
The mobile app SHALL provide each center's public name, categories, permitted location, description, public contacts, available verification and current needs, together with the center-directory-details contract. Optional fields SHALL preserve unknown/withheld meanings, and missing public location SHALL not prevent contact-only details. Private addresses, personal contacts and beneficiary records SHALL not be inferred or disclosed to complete the view.

#### Scenario: User opens a center
- **WHEN** the user selects a Kindness Center from the map or list
- **THEN** the app displays the center detail view with its permitted fields and explicit missing-data states

#### Scenario: Complete-looking profile lacks current evidence
- **WHEN** an imported center has an undated beneficiary count, a blank fee and an unsupported verified checkbox
- **THEN** details do not claim a current count, free service or official verification merely because those fields exist in the source
