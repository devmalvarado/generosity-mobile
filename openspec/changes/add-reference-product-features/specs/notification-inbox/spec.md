## Purpose
Provide the reference's bell destination with truthful notification history, read state and safe links, independently of operating-system push permission.

## ADDED Requirements

### Requirement: Bell and Notification List
The bell SHALL open a localized list of actual eligible notices, ordered newest first with stable IDs, timestamps, categories and read state. Unread indicators SHALL derive from those records, not fabricated counts. Loading, empty, offline and unavailable states SHALL be distinct. Push delivery success SHALL not be required to read an existing in-app notice.

#### Scenario: Push permission denied
- **WHEN** a signed-in user denies OS notification permission and opens the bell
- **THEN** their authorized in-app notices remain readable without falsely claiming push delivery

### Requirement: Ownership and Guest Isolation
Server notices and read state SHALL be private to the owning account and synchronize across its devices. Guest notices SHALL be limited to local eligible public alerts and remain separate from account history. Local proximity notices SHALL not upload a location trail. Sign-out SHALL clear private views and prevent a new account from inheriting prior notices or pending read operations.

#### Scenario: Account switch after reading a notice
- **WHEN** user A signs out and B opens the inbox
- **THEN** B receives only B's notices and no queued read operation from A is attributed to B

### Requirement: Idempotent Read and Retention
Opening a notice or explicitly marking notices read SHALL update desired read state idempotently, with pending status while offline. Duplicated delivery events SHALL not duplicate inbox items. Notice retention SHALL be documented; removed/expired records SHALL stop contributing to unread counts without deleting their referenced contribution history.

#### Scenario: Read acknowledgement is lost
- **WHEN** a read request commits and is retried after interruption
- **THEN** the notice remains read once and the unread count does not drift or become negative

### Requirement: Safe Targets and Preferences
Notice navigation SHALL re-resolve current public/private targets and enforce ownership. Removed or cancelled content SHALL explain its current state rather than allow invalid actions. Urgent/nearby/reminder preferences SHALL govern their respective optional notices; transactional history notices SHALL be distinguished from promotional alerts. In-app reads SHALL never silently enable push, location or reminder permissions.

#### Scenario: Open a cancelled event notice
- **WHEN** an event linked by a notice has been cancelled
- **THEN** the app shows cancellation and current enrollment state without offering a new reservation or exposing another attendee's data

### Requirement: Common Event Across In-App and System Presentation
Eligible backend events SHALL share stable IDs across owned inbox items, foreground banners and OS alerts. Clients SHALL reconcile current data on receipt/resume/refresh without duplicating in-app notices. Provider submission or OS presentation SHALL NOT mark notices read. Payloads SHALL omit beneficiary data and private notes; targets SHALL resolve allowlisted entity IDs under the current session.

#### Scenario: Retry or foreground receipt
- **WHEN** the same event arrives repeatedly while the app is foregrounded
- **THEN** the inbox retains one item and the app does not append duplicate banners or increment unread counts for that event

#### Scenario: Missed phone notification
- **WHEN** the OS denies or delays a push and the user later resumes the app
- **THEN** synchronization retrieves the authorized notice and current target state without claiming that the phone displayed the alert

#### Scenario: Notification belongs to the previous account
- **WHEN** a previously delivered notification is opened after an account switch
- **THEN** the app resolves its target under the current account and does not reveal the previous account's private notice or target
