## Purpose
Bring the existing Generosity native screens closer to the supplied visual reference through consistent branding, layout and hierarchy, without adding the concept's unapproved functions or inventing product data.

## ADDED Requirements

### Requirement: Shared Design Source Across Native and Web
Android, iOS, the public/center portal and hosted account/legal pages SHALL
derive Generosity color roles, font configuration and common dimensions from
one versioned design file. Native components SHALL retain their platform
behavior and text scaling. Google/Apple branded controls SHALL retain provider
styling. Logo masters SHALL be separate editable vectors; a single generation
command SHALL update platform assets and web exports without editing screens.

#### Scenario: Change the brand's primary action color
- **WHEN** a maintainer changes the shared action color and regenerates resources
- **THEN** Android, iOS, portal and account actions consume the new value, with light/dark text contrast checked before publication

#### Scenario: Change typography or a logo
- **WHEN** configured fonts or a master logo are changed and resources are regenerated
- **THEN** existing screen components consume the new font configuration or logo resources, native text remains scalable, and unsupported or uninstalled font/asset inputs are not advertised as verified

#### Scenario: Prevent stale platform output
- **WHEN** a generated theme or brand asset differs from its source configuration
- **THEN** native builds and web publication checks fail with a regeneration instruction, without modifying production or silently publishing only one platform's theme

### Requirement: Validated Build-Time Design Resources
Generation SHALL validate semantic light/dark color pairs, font configuration,
common dimensions and SVG masters. Configured normal-text pairs SHALL meet
4.5:1 contrast and touch targets SHALL be at least 48dp Android / 44pt iOS /
44px web. Native builds and web/API publication SHALL reject generated-resource
drift. Installed mobile versions SHALL receive regenerated themes through app
updates.

#### Scenario: Reject an unreadable action color
- **WHEN** a proposed primary action color leaves its configured text below 4.5:1 in either appearance
- **THEN** design validation fails and the maintainer must correct the pair before the build/publication check can pass

#### Scenario: Keep hosted authentication resources scoped
- **WHEN** the account and legal pages load the shared theme
- **THEN** they consume their same-origin generated CSS without importing the landing's remote font stylesheet or changing provider-branded buttons

#### Scenario: Rebuild a brand change consistently
- **WHEN** logo masters or shared tokens change
- **THEN** the maintainer regenerates and commits affected native/web resources together, and stale declarations or manifest-tracked assets fail the read-only consistency check

#### Scenario: Reject unsupported source assets
- **WHEN** a color is not a six-digit sRGB value or a master SVG contains unsupported elements or transforms
- **THEN** generation fails explicitly without accepting the input as a valid design resource

### Requirement: Reference-Aligned Visual Language
Both native apps SHALL use the reference's Generosity wordmark, light neutral surfaces, dark readable text, orange primary/selected accents and distinct category cues. Typography, spacing, icon weight and control styling SHALL be consistent across discovery, details, needs, profile and settings. Welcome SHALL retain its separate mobile-welcome contract. Device frames and promotional content SHALL not become app UI.

#### Scenario: Compare native screen families
- **WHEN** welcome, map, detail, current needs and profile screenshots are reviewed together against the reference
- **THEN** brand, color roles and content hierarchy are recognizable across both apps, while native controls remain platform-appropriate

### Requirement: Preserve Existing Destinations and Actions
Visual changes SHALL preserve Map, List, Profile and Settings access, current search/filter semantics and existing authorized actions. Profile SHALL retain impact/history; needs SHALL remain within center details. Restyling SHALL NOT introduce the concept's global needs tab, separate impact tab, central plus action, favorites, ratings, achievements or streaks. Visible controls SHALL have supported actions and accessible names.

#### Scenario: Navigate after restyling
- **WHEN** a user opens discovery, chooses a center, inspects needs and returns to the list or map
- **THEN** the existing destinations and back behavior remain available, selected filters/query are retained, and language/notification settings remain reachable

#### Scenario: Unsupported reference control
- **WHEN** the image contains a heart, share, bell or plus control without a scoped working action
- **THEN** the app does not render a dead or pretend-functional control merely to match the image

### Requirement: Map-Led Discovery Composition
Discovery SHALL prioritize the geographic map beneath a compact branded header, search and category controls. Selection SHALL be distinguishable and present a compact center preview with available identity/category/distance information and access to existing details. Controls and preview SHALL not obscure each other or required map attribution. The existing list alternative and unavailable-location behavior SHALL remain usable.

#### Scenario: Selected center on a small phone
- **WHEN** a user selects a map marker at normal text size
- **THEN** the center preview, search/filter controls and a usable map area coexist within safe bounds, with distinguishable selection and a working detail action

#### Scenario: Map or location is unavailable
- **WHEN** map loading or location access fails
- **THEN** the same visual system presents the existing list/manual-location fallback without drawing a fake geographic map or invented user position

### Requirement: Center Detail Hierarchy
Center details SHALL prioritize identity, available category/location metadata, description, current needs and supported help actions in that order. An approved center photo, when available, SHALL precede identity without obscuring content. A missing photo SHALL use a compact non-photographic identity treatment. Primary help actions SHALL be visually prominent while contact, visit and verification information remain accessible and truthful.

#### Scenario: Detail with or without a photo
- **WHEN** a center detail is shown using supported metadata
- **THEN** its name and purpose lead into needs and help actions, with no blank photo-sized gap, unrelated building photo, invented opening status or sample rating

#### Scenario: Long content and help actions
- **WHEN** a center has a long localized name and several needs
- **THEN** content scrolls without clipping and help actions remain reachable without covering the final need or platform navigation

### Requirement: Compact Need Presentation
Existing current-need rows SHALL use a consistent method/category icon, readable title, concise description, labeled priority and available quantity/unit information. Priority SHALL not depend on color alone. Rows SHALL follow existing shared ordering/expiry rules and preserve supported help actions. The reference's cross-center feed and All/Urgent/Nearby controls SHALL not be added by this visual change.

#### Scenario: Multiple priorities in a center detail
- **WHEN** current needs include urgent, high, medium and normal priorities
- **THEN** their rows share an aligned layout with explicit priority labels and distinct cues, while the existing ordering and contribution actions remain unchanged

### Requirement: Profile and Impact Composition
Profile SHALL present account identity or guest state first, followed by a clearly labeled impact summary and contribution history. Available metrics SHALL have names, units and provenance instead of an unlabeled number string. A points treatment SHALL show only supported informational data or an explicit unavailable state. Missing totals SHALL not be populated with reference values, entry counts posing as hours, or fabricated monetary balances.

#### Scenario: Registered user with contribution history
- **WHEN** profile displays authoritative impact and history
- **THEN** compact metrics distinguish confirmed/completed and self-reported activity using the existing contracts, without adding level, streak or achievement panels

#### Scenario: Guest or unavailable metrics
- **WHEN** profile belongs to a guest or has no supported value for a metric
- **THEN** the screen shows the corresponding empty/unavailable state rather than the reference's sample person, 2450 points, hours or currency balance

### Requirement: Responsive Accessible Presentation
All affected native screens SHALL remain usable in Spanish/English on small phones, with enlarged system text and safe-area insets. Text and controls SHALL not overlap or clip; metric rows and action groups SHALL reflow when necessary. Touch areas SHALL meet platform minimums, icon-only actions SHALL be named, and status/selection SHALL remain distinguishable without color. The visual change SHALL preserve existing supported appearance modes.

#### Scenario: Enlarged Spanish text
- **WHEN** a small phone uses enlarged system text with long Spanish labels and the keyboard open in search
- **THEN** fields, errors, filters, navigation and actions remain reachable without hiding essential content or losing input

#### Scenario: Nonvisual navigation
- **WHEN** a screen-reader user moves through discovery, needs and profile
- **THEN** meaningful content, selection, priority, units and actions are announced in an order matching the visible hierarchy, without duplicate decorative-icon announcements

### Requirement: Honest Visual States and Assets
Loading, empty, offline, error and unavailable states SHALL use the same visual hierarchy as loaded content. Branded assets SHALL have approved provenance; center photos and avatars SHALL represent their actual subjects when available. Test fixtures SHALL not become production claims. Visual parity SHALL not bypass authentication, payment, permission or contribution-state rules.

#### Scenario: Failed request during a styled flow
- **WHEN** an existing data or contribution request fails
- **THEN** the restyled screen shows the actual error/pending state and preserves the existing retry/input behavior instead of a success panel copied from the reference
