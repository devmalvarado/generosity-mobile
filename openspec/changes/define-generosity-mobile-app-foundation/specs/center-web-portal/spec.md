## Purpose
Let authorized Kindness Center staff manage published needs and incoming contributions from a phone browser, alongside a truthful public Generosity landing, using the same backend as the native apps.

## ADDED Requirements

### Requirement: Public Landing and Staff Entry
The public landing SHALL remain accessible without an account and expose a center sign-in route. Published data SHALL come from the shared catalog or be explicitly labeled demonstration content. The landing SHALL NOT claim a submission, location, review, verification or payment occurred without evidence. Unavailable actions SHALL explain their state without fabricating success.

#### Scenario: Existing demonstration form
- **WHEN** a visitor submits a form without a configured operational endpoint
- **THEN** the page explains that nothing was sent or published and retains the entered values without displaying a receipt or clearing the form

#### Scenario: Public visitor reaches center entry
- **WHEN** an unauthenticated visitor chooses to publish a need
- **THEN** the site routes to real sign-in while preserving the intended destination, without granting publishing permission

### Requirement: Illustrated Helping Journey
The public landing SHALL explain center publication, an iPhone notification and partial help confirmed by the center, using shared branding, Spanish/English text and accessible captions. The sequence SHALL reflow on phones and at 200 percent text size. Illustrations SHALL NOT submit data, request notification permission or represent live records or proof of push delivery.

#### Scenario: Visitor learns how a need receives help
- **WHEN** a visitor opens the landing's how-it-works section
- **THEN** a three-step illustration explains publication from the center portal, an alert with notifications enabled, and progress updated after the center confirms receipt of a partial contribution
- **AND** the real helping action leads to public discovery rather than submitting the illustrated offer

### Requirement: Staff workspace path
The landing's center entry SHALL open `/kindness-center`. That path SHALL be the staff workspace, not public helping. Choosing the landing's help action SHALL NOT send the visitor there. A misspelled `kidness-center` path SHALL NOT be published as an alias.

#### Scenario: Visitor opens the center entry
- **WHEN** a visitor chooses the landing's center entry
- **THEN** the browser opens `/kindness-center` and that visit is not recorded as a help offer

#### Scenario: Helper opens the staff path
- **WHEN** a signed-in person without a center assignment opens `/kindness-center`
- **THEN** the page explains that access is not granted and shows no publishing controls

### Requirement: Dedicated Web Journeys
The landing SHALL present entry links instead of embedding forms or account controls. Registration SHALL use `/registrar-sede/`, staff operations `/kindness-center/` and discovery/offers/history `/ayudar/`. Routes SHALL support direct opening, reload and language selection while initializing only their own components. Legacy fragments SHALL redirect without losing their record or destination. Hosted sign-in SHALL preserve the originating form without automatically submitting it.

#### Scenario: Visitor follows an old need link
- **WHEN** a visitor opens a landing URL with a `#necesidad/...` fragment
- **THEN** the browser opens the same need under `/ayudar/`, preserving the query and fragment, without exposing a staff editor or sending an offer

#### Scenario: Organization works without navigating the landing
- **WHEN** a representative directly opens or reloads `/registrar-sede/` or `/kindness-center/`
- **THEN** the relevant account and form controls are available independently of the landing, retaining existing account isolation, membership, CSRF and retry boundaries

### Requirement: Public Interactive App Tour
The public `/app/` tour SHALL present all 34 reference screens in Spanish/English, light/dark appearance and mobile navigation with shared branding. It SHALL explain current and planned capabilities without specification names, task IDs or repository-document links. Tour actions SHALL remain in memory without authentication, API mutations, location requests or payments. The tour SHALL distinguish exploration from operational actions and link back to real participation routes.

#### Scenario: Visitor explores a capability
- **WHEN** a visitor selects a screen, changes language or opens a simulated action
- **THEN** the tour renders the corresponding localized screen without an API mutation, credential request or technical specification link

#### Scenario: Static release includes dedicated routes
- **WHEN** the portal is packaged for production or the isolated preview environment
- **THEN** its explicit public-file inventory includes every route and required asset, excludes repository/private files, and preview API metadata is applied to every operational route

### Requirement: Public Tour Module Delivery
The production host SHALL serve the tour entry module and its imports with a JavaScript MIME type accepted by browsers. Release acceptance SHALL verify their HTTP content types and actual rendered navigation on the published domain; file existence and a 200 response alone are insufficient. Module references SHALL use current asset versions.

#### Scenario: Public hosting executes the tour modules
- **WHEN** a visitor opens `/app/` on the published domain
- **THEN** the map, navigation and screen inventory render and changing screens works without a module MIME error

### Requirement: Center-Scoped Workspace
Only authenticated staff with an active backend-issued center assignment SHALL access that center's private workspace. The workspace SHALL show the active center and allow switching only among assigned centers. Missing or revoked access SHALL block reads and writes. Sign-out SHALL clear private views and account-scoped browser state; account registration SHALL NOT grant a center role.

#### Scenario: Attempt to manage another center
- **WHEN** staff assigned only to center A modifies a URL or payload to reference center B
- **THEN** the backend denies the request without exposing B's private drafts or contributions

#### Scenario: Account lacks a center assignment
- **WHEN** a signed-in person has no assigned center
- **THEN** the portal shows an access-required state and no publishing controls or self-service role assignment

### Requirement: Need Authoring and Validation
Staff SHALL author a title, description, supported time/goods help methods, priority and closing date/time for the active center. Goods SHALL require a positive target and unit; time needs SHALL describe the activity and availability. The form SHALL show the closing timezone, use documented limits and field errors, and accept supplied Spanish/English content without requiring both translations. Money publication SHALL remain unavailable without an approved payment integration.

#### Scenario: Valid goods or time need
- **WHEN** staff enters a goods target/unit or time activity/availability with a future closing instant
- **THEN** the form presents a preview of the public content and the backend validates the same fields before accepting the write

#### Scenario: Invalid or incomplete input
- **WHEN** the title is blank, the quantity is nonpositive, the closing time is invalid/past or a documented field limit is exceeded
- **THEN** submission fails with associated field errors and all other entered values remain available for correction

### Requirement: Private Drafts and Reliable Publication
Staff SHALL be able to save private drafts and explicitly publish valid needs. Saved and published states SHALL require durable backend acknowledgement. Retries SHALL use stable operation IDs; stale versions SHALL require reconciliation. Network failure or session expiry SHALL preserve entered work within the current account context without implying publication, and reauthentication SHALL NOT automatically submit it.

#### Scenario: Draft survives a browser restart
- **WHEN** staff saves a draft successfully and later signs in again to the same center
- **THEN** the draft remains editable while anonymous catalog queries and mobile discovery exclude it

#### Scenario: Publication response is lost
- **WHEN** the server commits publication but the browser loses the response and retries
- **THEN** the portal resolves the original outcome and displays one published need with its authoritative ID and version

#### Scenario: Session expires while editing
- **WHEN** submission fails because the current session expired
- **THEN** no success is shown, reauthentication preserves the same account's form, and a different account cannot view or submit that private work

### Requirement: Safe Need Maintenance
Staff SHALL list their center's drafts and published needs, edit permitted fields and explicitly close a published need to new offers. Edits SHALL use expected versions and preserve accepted contributions and history. The backend SHALL reject targets below committed goods and changes to units/help methods that invalidate accepted offers. Closing SHALL NOT claim delivery or silently cancel contributions, and SHALL not delete history.

#### Scenario: Concurrent offer makes a target invalid
- **WHEN** staff reduces a target below the quantity committed before the edit transaction commits
- **THEN** the backend rejects the edit, returns current capacity and preserves the existing need and contribution ledger

#### Scenario: Close a need with pending offers
- **WHEN** authorized staff closes an active need after acknowledging its outstanding offers
- **THEN** new offers are rejected while existing offers retain their states and remain available for permitted confirmation, fulfillment or owner cancellation

#### Scenario: Two staff edit the same version
- **WHEN** a second edit is submitted after another staff member has updated the need
- **THEN** the portal shows a conflict with current server data and preserves the unsaved edit instead of silently overwriting the newer version

### Requirement: Shared Catalog Publication
Accepted public need changes SHALL advance the backend catalog revision used by the landing and both native apps. Drafts SHALL never enter public responses or urgent-alert events. Publication, update and closure SHALL reuse the established freshness, capacity, expiry and notification rules; the portal SHALL not maintain a separate production needs list.

#### Scenario: Publish from a phone and discover in the apps
- **WHEN** staff publishes a need from the portal and Android/iOS refresh the catalog
- **THEN** both apps display the same need ID, content, priority, expiry and available quantity, and eligible urgent notifications derive from the accepted publication event

### Requirement: Center Contribution Actions
The workspace SHALL show a paginated list/detail of contributions to the active center and expose only server-permitted confirmation and fulfillment actions. Version conflicts SHALL refresh the current state without duplicate transitions. Staff SHALL see only donor information needed for those contributions, not unrelated personal activity, history or visit coordinates. Personal help SHALL remain self-reported and outside center certification.

#### Scenario: Staff fulfills an accepted contribution
- **WHEN** authorized staff confirms an offered contribution and later marks the confirmed contribution fulfilled
- **THEN** the existing lifecycle API records actor/time and mobile history/impact synchronize the authoritative result once

#### Scenario: Donor cancels before staff confirms
- **WHEN** a donor cancellation commits before staff submits confirmation against the earlier version
- **THEN** the portal shows the cancelled state and cannot resurrect the contribution or count it as completed

### Requirement: Mobile-First Operability
Landing, sign-in, lists, editors and contribution actions SHALL be usable at 320 CSS pixels and wider without horizontal page scrolling, clipped content or overlapping controls. Primary touch targets SHALL be at least 44 by 44 CSS pixels. Text at 200 percent, portrait/landscape and the virtual keyboard SHALL preserve access to fields and actions. Wider layouts SHALL retain the same operations without requiring hover.

#### Scenario: Publish on a narrow phone
- **WHEN** staff completes a need form at 320 or 390 CSS pixels with long Spanish text and the keyboard open
- **THEN** labels, errors, preview and submit controls remain reachable in a single-column flow without zooming out or losing entered data

#### Scenario: Reflow and larger screens
- **WHEN** the site is used with enlarged text or at tablet/desktop widths
- **THEN** navigation, lists and actions reflow without truncating essential information or hiding operations available on phones

### Requirement: Accessible Localized Web States
The site SHALL resolve Spanish/English from ordered browser preferences by default, fall back to English and persist a system/en/es override per browser. Controls, dates, errors and empty/loading/offline states SHALL use the effective locale. Inputs SHALL have associated labels/errors; navigation and dialogs SHALL support keyboard focus and screen readers. Center-authored content SHALL use existing language fallback without machine translation.

#### Scenario: First visit and language override
- **WHEN** an English-preferring browser opens the site and the user selects Spanish in settings
- **THEN** the UI changes without losing form input, persists that browser's choice and can return to system mode without changing another device's preference

#### Scenario: Keyboard and screen-reader submission
- **WHEN** staff navigates the mobile menu and submits an invalid form using keyboard or screen reader
- **THEN** hidden navigation is not focusable, the menu state is announced, errors are associated with their fields and focus leads to actionable feedback

### Requirement: Safe Public Content
Center-authored content SHALL render as inert text, not executable markup, in the portal, landing and API-consuming views. Public responses SHALL exclude draft content, staff identities and donor notes. Center access or publication SHALL NOT imply official verification or enable payments.

#### Scenario: Malicious description or public query
- **WHEN** a saved description contains script markup or an anonymous visitor requests a need
- **THEN** scripts do not execute and only published public fields are returned, without leaking private draft revisions or contribution details
