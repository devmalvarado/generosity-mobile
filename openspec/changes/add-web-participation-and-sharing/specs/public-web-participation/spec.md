## Purpose
Let people discover and participate in helping from a phone browser without installing an application, while sharing authoritative identity, capacity and history with native clients.

## ADDED Requirements

### Requirement: Public Browser Discovery
The web SHALL expose current public centers, needs, campaigns, events and sanitized meal opportunities without account creation, installation, push or location permission. Links SHALL open the requested content directly, with search/list alternatives to maps. Drafts, recipients, household cases and participant identities SHALL not enter public HTML, metadata or cached responses.

#### Scenario: Open an invitation without an app
- **WHEN** a guest opens a current opportunity URL on a phone
- **THEN** details and available helping actions appear without an install wall or unrelated landing-page detour

### Requirement: Authenticated Browser Helping
Users SHALL sign in through the configured identity provider only when protected participation requires it, retaining the chosen opportunity and non-sensitive form input. Browser users SHALL submit time/goods offers, event enrollment, meal commitments and personal help using the same rules as native clients. Authentication SHALL not submit automatically; monetary actions SHALL retain provider release gates.

#### Scenario: Return from sign-in to a now-full event
- **WHEN** a guest signs in after choosing an event whose last seat has since been taken
- **THEN** the browser revalidates availability and offers an honest full/waitlist state rather than claiming a reservation

### Requirement: Shared Capacity and Owned History
Browser/native requests SHALL share server transactions, operation identities and account-owned history. Browser users SHALL inspect and cancel permitted commitments, read their inbox, and manage preferences without installing an app. Pending or failed requests SHALL not appear completed. Repeating a request after a lost response SHALL not consume a second seat or quantity.

#### Scenario: Reserve concurrently from web and Android
- **WHEN** the same account submits an enrollment from both clients
- **THEN** one enrollment exists and both clients reconcile to its authoritative state

### Requirement: Optional Progressive Installation
Installation SHALL be optional and never block an action. Where supported, the web SHALL offer an installable experience with truthful offline states. Offline cached public content SHALL show freshness; availability and protected writes SHALL require server confirmation. Service-worker caches SHALL exclude private responses, credentials and sensitive fieldwork. Declined installation or unavailable push SHALL leave core web participation usable.

#### Scenario: Offline page shows an old open slot
- **WHEN** a cached opportunity is opened offline
- **THEN** it is marked stale and no slot is shown as newly reserved until the server acknowledges a later explicit submission

### Requirement: Secure Accessible Phone Flows
The web SHALL use server-enforced authorization, secure sessions, CSRF protection and safe redirects. Sign-out/account change SHALL clear private views and caches. All flows SHALL work from 320 CSS pixels with enlarged text, keyboard/screen-reader support and no hover dependency. Spanish/English SHALL follow browser preferences with a persisted override; low-bandwidth list views SHALL not require loading the map.

#### Scenario: Shared browser changes accounts
- **WHEN** one user signs out and another signs in
- **THEN** prior private history, input and notification data are not visible through back navigation, caches or pending callbacks

#### Scenario: Small phone without location access
- **WHEN** a user browses in Spanish at 320 CSS pixels with location denied
- **THEN** manual-area search and readable participation forms remain usable without horizontal page overflow
