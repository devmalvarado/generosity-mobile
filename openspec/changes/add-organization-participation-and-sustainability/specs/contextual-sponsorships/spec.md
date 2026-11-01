## Purpose
Offer modest, disclosed sponsorship placements that support platform costs without obstructing helping or exposing sensitive user information.

## ADDED Requirements

### Requirement: Reviewed Disclosed Placement
Each placement SHALL identify advertiser, localized message, destination, approved surface and start/end dates. Publication SHALL require reviewed content and an active commercial entitlement. Every impression SHALL display a clear Spanish/English advertising or sponsorship label. Paying SHALL not create a center endorsement or official verification claim.

#### Scenario: Paid creative has not been reviewed
- **WHEN** its billing entitlement becomes active
- **THEN** the creative remains unpublished until content review passes

### Requirement: Non-Blocking Inventory
Placements SHALL be limited to configured public discovery/directory surfaces with a published per-view/frequency limit and an accessible dismissal control. They SHALL not cover navigation, replace a required action, autoplay sound or require viewing/clicking to help. Authentication, contribution submission, private referrals and medicine/case flows SHALL contain no advertisements.

#### Scenario: Sponsorship service fails on a phone
- **WHEN** the ad cannot load or the user dismisses it
- **THEN** the underlying helping flow remains fully usable without a blocking placeholder or repeated forced display

### Requirement: Context Without Sensitive Targeting
Initial placement selection SHALL use approved page topic or explicitly selected public area only. It SHALL not use household records, health needs, private contact, precise device location, personal helping history or employment behavior. Ad SDKs, cross-site tracking and advertiser access to participant data SHALL not be introduced by this capability.

#### Scenario: Advertiser requests people who sought medicine
- **WHEN** an advertiser attempts to define that audience
- **THEN** the request is rejected and no case information or inferred health audience is disclosed

### Requirement: Commercial Metrics Stay Separate
Impression/click reports SHALL use documented deduplication and aggregate-only reporting with bounded retention. Missing or unmeasured delivery SHALL be unavailable, not estimated as actual. Ad views, clicks and sponsor spend SHALL not create helping points, validated impact or a higher organic discovery rank.

#### Scenario: A person clicks the same sponsorship repeatedly
- **WHEN** delivery metrics update
- **THEN** the reporting rule limits duplicate activity and helping/award ledgers do not change

### Requirement: Safe Creative Lifecycle
Creative and destination changes SHALL return to review before publication. Reviewers SHALL be able to pause a placement with a recorded reason and a private appeal/support route. Expired, revoked or unpaid placements SHALL stop serving, including cached versions. Clients SHALL validate configured destinations and render inert content without arbitrary advertiser scripts.

#### Scenario: Destination changes after approval
- **WHEN** a sponsor replaces its approved URL
- **THEN** the new destination is not served until reviewed and the change is auditable

### Requirement: Accessible Cross-Platform Disclosure
Approved placements SHALL provide equivalent labels, destination context, readable text and dismissal on web, Android and iOS. Layout SHALL remain usable at 320 CSS pixels and enlarged text; screen readers SHALL distinguish sponsored content from organic helping opportunities. Unavailable sponsorship data SHALL never block public content.

#### Scenario: Screen reader reaches a sponsor card
- **WHEN** a visitor navigates its controls
- **THEN** the sponsorship label, advertiser, destination and dismiss action are announced without implying a recommended care service
