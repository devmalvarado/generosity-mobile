## Purpose
Let users save and revisit centers through consistent favorite controls, with durable private ownership and safe guest-to-account transitions.

## ADDED Requirements

### Requirement: Consistent Favorite Controls
Center previews and details SHALL expose a named favorite control with the current saved state. Profile SHALL provide a saved-center list. Updating one surface SHALL update the others for the same center. Favoriting SHALL not imply a donation, verification, public endorsement or subscription to alerts.

#### Scenario: Save from a map preview
- **WHEN** the user saves a center and later opens its details or saved list
- **THEN** the same center appears saved once and its accessible control offers removal

### Requirement: Durable Guest and Account Ownership
Guest favorites SHALL persist locally without registration. Account favorites SHALL be private to their owner and synchronize across signed-in devices. On sign-in, transferring guest favorites SHALL require explicit choice and deduplicate IDs. Signing out SHALL hide account favorites without exposing them to a later guest or account.

#### Scenario: Decline guest import
- **WHEN** a guest signs in and declines to transfer local favorites
- **THEN** those centers are not uploaded or merged into the account's saved list

#### Scenario: Switch accounts
- **WHEN** account A signs out and account B signs in
- **THEN** B cannot read or submit A's saved centers or pending favorite operations

### Requirement: Idempotent Offline Changes
Save/remove requests SHALL express the desired state, use stable operation IDs and support versioned conflict reconciliation rather than blind toggles. Offline changes SHALL remain visibly pending until acknowledged. Retries SHALL not reverse an already committed choice, and an unresolved cross-device conflict SHALL show authoritative state and permit explicit reapplication.

#### Scenario: Retry a lost save response
- **WHEN** a save commits but its response is lost and the client retries
- **THEN** the center remains saved once instead of toggling back to unsaved

### Requirement: Unavailable Saved Centers
The saved list SHALL distinguish a removed/unavailable center from a temporarily failed refresh. Users SHALL be able to remove an unavailable saved item. Favorite deletion SHALL not delete contribution history or change the public catalog.

#### Scenario: Saved center is removed from the catalog
- **WHEN** the saved list refreshes after a center is removed
- **THEN** it shows an unavailable item with a remove action, without offering help against the removed center
