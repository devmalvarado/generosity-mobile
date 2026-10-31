## Purpose
Provide the reference's native navigation and helping entry points while retaining existing discovery, settings, history and account access.

## ADDED Requirements

### Requirement: Reference Destinations
Both apps SHALL expose Map, Needs, Impact and Profile as labeled primary destinations with a central helping action distinct from a tab. Map SHALL retain a map/list mode using the same search and filters. Profile SHALL expose settings, saved centers and history; Needs SHALL expose events; Impact SHALL expose all achievements. Selection and accessible names SHALL remain clear on small screens and enlarged text.

#### Scenario: Migrate existing destinations
- **WHEN** the new navigation replaces the foundation's Map/List/Profile/Settings layout
- **THEN** center list is available from Map, settings/history from Profile, and no saved data or previous discovery capability is lost

#### Scenario: Explore as a guest
- **WHEN** an anonymous user visits each primary destination
- **THEN** discovery, needs and event browsing remain open while private impact/profile content shows an honest guest state with optional account entry

### Requirement: Central Helping Choice
The central action SHALL offer choosing a published need or recording personal help. Choosing a need SHALL open the needs flow; personal help SHALL use the existing self-reported form. Writes SHALL require real authentication, preserve the intended route/input through sign-in, and revalidate current eligibility. The action SHALL never publish center content or award points merely for opening it.

#### Scenario: Guest chooses personal help
- **WHEN** a guest selects personal help and completes sign-in
- **THEN** the app resumes that form without submitting automatically, and saved activity remains labeled self-reported

#### Scenario: Cancel the choice
- **WHEN** a user dismisses the helping chooser or cancels authentication
- **THEN** the previous destination and its state remain available without creating any contribution

### Requirement: State-Preserving Routes
Tab changes, detail navigation and incoming links SHALL retain appropriate query/filter/scroll state and obey the current account's ownership. A back action SHALL return to the originating flow. Selecting a tab SHALL not repeat writes or subscriptions. Removing a referenced record SHALL produce an unavailable destination, not an unrelated sample item.

#### Scenario: Return from a need detail
- **WHEN** a user opens a filtered feed item, inspects its center and returns
- **THEN** the selected feed mode and query remain intact without duplicate offers or resetting the user's origin

#### Scenario: Account changes during private navigation
- **WHEN** an account signs out while a private history or impact screen is open
- **THEN** its private content is cleared and the destination resolves to the guest state rather than another account's cache

### Requirement: Contextual Center Help
Center detail's primary helping action SHALL lead to choosing one of that center's current needs. Method-specific time/goods/money actions SHALL retain the center and filter to compatible needs, with money gated by provider readiness. Neither entry SHALL silently select a different center, invent availability or submit fixed values. A method with no eligible need SHALL show an honest unavailable state.

#### Scenario: Choose goods from a center detail
- **WHEN** a user chooses the goods help method
- **THEN** they choose a current compatible need of that center and enter their own quantity before the existing authenticated submission flow

### Requirement: Account Summary
Profile and the Impact account header SHALL show the owning account's display name, optional approved avatar and localized membership month/year from authoritative account creation time. Missing metadata SHALL be omitted or use neutral initials, not reference identities. Level SHALL come from gamification data. Public reviews SHALL not inherit private profile fields without explicit publication consent.

#### Scenario: New account without an avatar
- **WHEN** a newly registered user opens their account summary
- **THEN** the app uses their actual name/date or available neutral fallback, not the reference's sample person, join date or level

### Requirement: Scoped Navigation Rollout
The functional navigation SHALL activate only with usable destination routes and honest empty/error states. It SHALL supersede the visual-only preservation of old routes at this phase, not during the foundation's styling task. Existing data-state, accessibility and Spanish/English requirements SHALL apply to all new destinations.

#### Scenario: Styling is delivered before functional routes
- **WHEN** only the foundation's visual alignment is implemented
- **THEN** existing navigation remains operational until this change's route migration is implemented and verified
