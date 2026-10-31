## Purpose
Expose current needs across centers in the reference's Needs destination, with predictable filtering, location fallback and consistent contribution entry.

## ADDED Requirements

### Requirement: Public Current Needs Feed
The feed SHALL be available without registration and include only published, active, non-expired needs from the same authoritative catalog used by center details. Rows SHALL expose center identity, title, description, help methods, priority and applicable quantities; distance SHALL require a valid origin. Selecting a row SHALL open that need and its existing help flow.

#### Scenario: Catalog includes private or closed needs
- **WHEN** feed data includes drafts, covered, cancelled or expired records
- **THEN** those records are excluded and a current need uses the same ID and availability as its center detail

### Requirement: Mode and Filter Semantics
All/Urgent/Nearby SHALL be mutually exclusive modes. Urgent SHALL require urgent priority; Nearby SHALL require a valid origin and show only needs inside the displayed radius. Search SHALL match localized need/center text; selected categories SHALL match any selected category, combined with the mode and search. Clearing filters SHALL restore All without requesting location automatically.

#### Scenario: Combine category and urgency
- **WHEN** the user selects Urgent and a health category
- **THEN** only active urgent needs matching health and any entered search are returned, with an empty state when none qualify

### Requirement: Center-Scoped Continuation
The center detail's View all needs action SHALL open the feed constrained to that center. The constraint SHALL be visible and removable and SHALL combine with mode/search/category filters. Returning to the detail SHALL preserve center identity; a removed or empty center SHALL not silently substitute other centers' needs.

#### Scenario: View every current need of one center
- **WHEN** a user selects View all from a center's needs preview
- **THEN** the feed shows that center's current needs and identifies the active center constraint until the user explicitly clears it

### Requirement: Honest Origin and Distance
Nearby SHALL accept a permitted fresh location or a user-selected manual origin and SHALL identify which is being used. Missing, denied or stale location SHALL prompt an explicit retry/manual choice, not silently substitute a city. The radius SHALL be visible. Location used for the query SHALL not become a continuous tracking history or public profile field.

#### Scenario: Location permission is denied
- **WHEN** a user selects Nearby without a valid origin
- **THEN** the app explains the missing origin, permits manual selection or switching modes, and displays no fabricated distance

#### Scenario: Manual origin differs from device position
- **WHEN** the user searches around a manually selected place
- **THEN** distances and radius use that place and the interface identifies it rather than claiming the results are near the user's actual position

### Requirement: Stable Ordering and Pagination
All/Urgent SHALL sort by descending priority, then newest publication and stable ID. Nearby SHALL sort by distance, then priority and stable ID. Pagination SHALL use a consistent query/revision and deduplicate IDs; changing mode, origin or filters SHALL restart the query without mixing pages. Clients SHALL not present an incomplete cached subset as an exhaustive live search.

#### Scenario: Another page arrives after filters change
- **WHEN** an earlier request completes after the user changes category or origin
- **THEN** its rows do not enter the new result set and repeated page records are not duplicated

### Requirement: Freshness and Failure States
Loading, empty, cached/offline and failed refresh states SHALL be distinct and localized. Known expired/closed needs SHALL stop accepting offers even in cached results. A stale selected need SHALL be revalidated by the backend before submission, preserving entered input when availability changes.

#### Scenario: Last available quantity is taken
- **WHEN** another donor covers a need after it appears in the feed
- **THEN** refresh removes it from current results and an in-progress offer handles current server availability without fake acceptance
