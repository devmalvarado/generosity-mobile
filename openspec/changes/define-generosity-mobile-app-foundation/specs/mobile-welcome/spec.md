## Purpose
Define the first usable Generosity screen and its guest/account entry paths, following the user-supplied welcome reference without reproducing promotional framing.

## ADDED Requirements

### Requirement: Reference-Aligned Welcome
The welcome screen SHALL use the Generosity logo/wordmark, a light background, a short localized purpose statement, an orange primary guest action, a secondary sign-in action, a registration link and a helping illustration below the actions. It SHALL follow the welcome phone in docs/design-reference.md, not the surrounding advertisement.

#### Scenario: First launch without an entry choice
- **WHEN** a fresh installation finishes resolving its language and session
- **THEN** it shows the branded welcome with all three entry actions rather than immediately opening the map

#### Scenario: Visual acceptance on both platforms
- **WHEN** Android and iOS screenshots are compared with the supplied welcome reference
- **THEN** the brand, action hierarchy and illustration placement are recognizable without device frames, store badges or the promotional feature strip

### Requirement: Guest and Account Entry
The guest action SHALL open anonymous discovery without authentication or a permission prompt. Sign-in and registration SHALL open their corresponding real identity flows. Cancelling either flow SHALL return to welcome without creating a demo identity.

#### Scenario: Continue as guest
- **WHEN** the user selects the primary guest action
- **THEN** the map/list discovery experience opens with search, filters and center details available

#### Scenario: Cancel registration
- **WHEN** the user cancels the registration flow
- **THEN** welcome remains usable and no registered profile or contribution is fabricated

### Requirement: Returning Entry State
The app SHALL persist a successful guest entry choice and restore a valid signed-in session. Subsequent launches SHALL enter discovery after state restoration, without forcing welcome on every launch. Explicit sign-out SHALL return to guest-capable welcome and hide private account data.

#### Scenario: Returning guest
- **WHEN** a user who previously continued as guest restarts the app
- **THEN** discovery opens in anonymous mode with saved local settings

#### Scenario: Returning registered user
- **WHEN** the app restores a valid session
- **THEN** it opens the authenticated experience without exposing another account's cached content

### Requirement: Localized and Accessible Welcome
Welcome SHALL use the resolved language before showing its copy, support English and Spanish, respect safe areas and enlarged text, and expose accessible action names. Text and controls SHALL remain readable and non-overlapping; vertical scrolling SHALL preserve access on small screens.

#### Scenario: Spanish device on first launch
- **WHEN** the device resolves to Spanish and no override exists
- **THEN** welcome and identity-entry actions appear in Spanish without an initial English screen

#### Scenario: Enlarged text
- **WHEN** a user enlarges system text on a small phone
- **THEN** all actions and legal links remain reachable, with no clipped labels or illustration obscuring controls

### Requirement: Real Legal Destinations
Welcome SHALL expose localized Terms and Privacy links to configured documents. Opening or closing them SHALL preserve the entry screen. Missing or unreachable documents SHALL produce an honest unavailable state rather than a placeholder destination or a claim of recorded consent.

#### Scenario: Inspect privacy before entry
- **WHEN** the user opens and closes Privacy
- **THEN** the configured policy is shown when available and welcome remains usable afterward
