## Purpose
Make public helping opportunities easy to invite others to through stable links, safe previews and printable QR codes without compulsory installation or unsolicited outreach.

## ADDED Requirements

### Requirement: Canonical Opportunity Links
Published needs, campaigns, event occurrences and sanitized meal opportunities SHALL have canonical HTTPS URLs with stable identity. Supported installed apps SHALL resolve matching destinations; a usable browser fallback SHALL always remain. Links SHALL grant no private access, role, authentication or automatic commitment. Invalid, removed or cancelled targets SHALL show current status without silently switching opportunities.

#### Scenario: Scan an old event poster
- **WHEN** someone scans a QR for a cancelled occurrence
- **THEN** the current cancellation is shown and any alternatives require an explicit new selection

### Requirement: Safe Public Previews
Link previews and printable invitations SHALL include only approved public title, image, area, date and helping purpose. Private meal details, household locations, roster data and account identifiers SHALL be excluded. Dynamic capacity shown on the destination SHALL be current; static graphics SHALL disclose their generation date rather than promise remaining seats. Private visibility changes SHALL invalidate hosted previews where controllable.

#### Scenario: Messaging service fetches preview metadata
- **WHEN** a crawler requests a meal-campaign URL
- **THEN** it receives sanitized public content with no recipient name, address, dietary details or sign-in token

### Requirement: Voluntary User-Controlled Sharing
Users SHALL choose whether and through which available channel to share, copy a link or download a QR invitation. The system SHALL not import address books, post automatically, require invitations to help or claim delivery from opening a share sheet. No sharing, click, install or referral SHALL award helping points or organizer eligibility.

#### Scenario: Dismiss the share sheet
- **WHEN** a user opens and then cancels sharing
- **THEN** no sent invitation, completed help or award is recorded

### Requirement: Invite to Participate Together
An invitation SHALL be able to reference the same public activity for friends or a group without reserving places for them. Every self-service participant SHALL confirm their own commitment against current capacity; assisted participation SHALL use its separate authorized flow. Public views SHALL not reveal who accepted an invitation or build a social graph without consent.

#### Scenario: Group invitation reaches a nearly full activity
- **WHEN** several invitees follow the same link
- **THEN** each sees actual availability and no invisible group reservation or unconsented roster is created

### Requirement: Accessible Measurable Entry
QR material SHALL also provide a readable URL and contact/assistance route when available. Spanish/English previews SHALL preserve public-content fallback. Optional campaign/channel attribution SHALL use opaque non-personal codes, bounded retention and aggregate reporting; it SHALL not identify the sender or recipient, bypass analytics consent, or enable open redirects.

#### Scenario: User declines optional analytics
- **WHEN** an invitee declines measurement and follows the opportunity link
- **THEN** participation still works and no optional person-level tracking is activated
