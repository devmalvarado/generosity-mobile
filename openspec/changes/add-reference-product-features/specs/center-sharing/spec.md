## Purpose
Share public center destinations safely through native sharing and mobile web fallback without exposing account or contribution information.

## ADDED Requirements

### Requirement: Public Center Share Payload
Center details SHALL offer the native share action for a canonical public center URL and localized public title. The payload SHALL omit account identifiers, tokens, exact user location, private notes and drafts. Sharing SHALL work anonymously and SHALL not award activity or report successful delivery merely because the share sheet opened.

#### Scenario: Cancel sharing
- **WHEN** a user opens and dismisses the share sheet
- **THEN** the original center stays selected and no contribution, notification or achievement is created

### Requirement: Native and Browser Resolution
Configured HTTPS links SHALL open the matching public center in an associated installed app when supported, otherwise a mobile-first public web detail. URLs SHALL use stable center identity and resolve under current catalog visibility. A missing center SHALL show an unavailable state without redirecting to a different center or requiring account creation for public data.

#### Scenario: Recipient has no installed app
- **WHEN** someone follows a shared center link in a browser
- **THEN** a usable public center page opens with current needs and available metadata rather than a forced app-install gate

#### Scenario: Removed center link
- **WHEN** a previously shared center is no longer public
- **THEN** the app or browser explains unavailability and exposes no private or draft content

### Requirement: Validated Link Handling
Incoming links SHALL accept only configured hosts, supported paths and valid public identifiers. Redirect destinations SHALL be constrained; incoming parameters SHALL never grant roles, authenticate an account or submit a donation. Invalid links and absent share-domain configuration SHALL show an honest unavailable state rather than generating a fake production URL.

#### Scenario: Crafted or unconfigured link
- **WHEN** a link uses an untrusted host, unsupported action or the installation lacks a configured public domain
- **THEN** no private navigation or write occurs and the user receives a safe error or unavailable share state
