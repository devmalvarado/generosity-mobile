## Purpose
Find nearby participating centers that can actually assess or receive a particular kind of assistance request, without confusing a catalog category with a current service commitment.

## ADDED Requirements

### Requirement: Explicit Service Participation
Authorized staff SHALL opt their center into versioned intake services with stable need-category codes and explicit roles: assess requests, receive goods, distribute assistance or coordinate an authorized provider. Catalog category, ratings, visits or ordinary center membership SHALL NOT imply participation or professional authority. Medicine services SHALL additionally satisfy their separate release gates.

#### Scenario: Nearby health center has not joined
- **WHEN** the nearest health-category center has not activated the requested intake service
- **THEN** it is not presented as accepting that referral or as a medicine collection point merely because it appears in the catalog

#### Scenario: Receiving is different from dispensing
- **WHEN** a center is approved only to coordinate a provider or receive permitted goods
- **THEN** discovery states that role and does not claim the center can assess, prescribe or dispense medicines

### Requirement: Current Public Service Details
Each published service SHALL expose permitted categories/items, excluded items, visit/phone channels, public contact, service-specific hours/timezone, access instructions, appointment requirements, availability and last review time. Unknown information SHALL remain unknown. Center-declared capacity or availability SHALL be labeled as such and SHALL NOT guarantee individual admission or stock.

#### Scenario: Collection hours differ from general opening hours
- **WHEN** a center is open but its requested service is closed or appointment-only
- **THEN** the map/list and contact view show the service restriction before a user sends a heads-up or directions

### Requirement: Suitable Center Matching
Map and list SHALL use the same selected service, intake channel and availability criteria. Nearest ordering SHALL compare eligible centers using a disclosed origin and distance method, not categories alone. Paused, stale or unconfirmed services SHALL not appear as ready to accept a request; alternatives needing a call SHALL be distinguishable. No suitable match SHALL produce an honest empty state.

#### Scenario: Closest center cannot accept the request
- **WHEN** a nearby center is paused and a farther center actively accepts the selected service
- **THEN** the eligible center is offered as the nearest matching option, without promising attendance, a route duration or successful assistance

#### Scenario: No suitable service is available
- **WHEN** no center satisfies the current criteria
- **THEN** discovery offers changing the area/criteria or public contact alternatives without inventing a receiving center or creating a referral

### Requirement: Provider-Neutral Private Discovery
Public map renderers SHALL receive only public center coordinates and display fields. Beneficiary location, case references, medication details and referral content SHALL NOT enter map/search-provider requests or public markers. Manual origin selection SHALL work without GPS; using device location SHALL require permission. Straight-line distance SHALL not be labeled travel time or proof of proximity.

#### Scenario: Search without location permission
- **WHEN** a helper selects a public area manually and searches for medicine assistance
- **THEN** matching uses that origin without tracking the person, and external map requests contain no private case or medicine information

### Requirement: Service Revalidation And Continuity
The backend SHALL recheck current service eligibility, channel and version when accepting a heads-up or assistance commitment. A paused or changed service SHALL reject unsupported new actions with alternatives and preserve existing case history. Changing center SHALL require an explicit choice and new recipient consent, not an automatic private-case broadcast.

#### Scenario: Service pauses after discovery
- **WHEN** a user submits a heads-up from a stale service view
- **THEN** the server rejects the unsupported action, updates availability and preserves the user's filters without forwarding personal information to another center

### Requirement: No-App Center Directions
Public center/service address, phone, hours and access instructions SHALL be viewable in Spanish/English without sign-in or installation and shareable as a readable link or printable summary. Anonymous browsing SHALL remain available when map tiles fail. Public directions SHALL exclude private referral fields and state when contact is required rather than implying an appointment.

#### Scenario: Person has no smartphone
- **WHEN** the helper gives written center directions or its public telephone number
- **THEN** the person can visit or call without an app, account, QR scanner or personal phone, and receives no false appointment guarantee
