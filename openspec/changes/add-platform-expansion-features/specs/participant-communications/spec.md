## Purpose
Support accountable activity coordination through scoped conversations and announcements without exposing private rosters or requiring external contact sharing.

## ADDED Requirements

### Requirement: Contextual Conversation Access
Authenticated participants and assigned coordinators SHALL exchange bounded text messages in an authorized need, contribution, event or meal context. Users SHALL not message arbitrary accounts through identifier guessing. Conversation access SHALL be evaluated on every read/write and assignment change. Organizational announcements SHALL not disclose their recipient list or turn private replies into group messages.

#### Scenario: Participant replies to an event announcement
- **WHEN** a participant responds
- **THEN** the reply is visible only in their permitted coordinator conversation, not to the event roster

### Requirement: Safe Message Content and Delivery
Messages SHALL render as inert content with bounded length, safe links and idempotent sending. Pending, sent and failed states SHALL reflect server acknowledgment; retries SHALL not duplicate messages. Private household fields, payment credentials and meal instructions SHALL not be automatically embedded; sensitive instructions SHALL stay in their assignment-checked workflow. Unreviewed arbitrary attachments SHALL remain unavailable.

#### Scenario: Offline message is retried after reconnect
- **WHEN** a pending message is sent twice with the same operation identity
- **THEN** one message appears and its final delivery state is reconciled

### Requirement: Consent and Notification Controls
Inbox/email/push delivery SHALL obey channel preferences, permissions, rate limits and quiet-time policy while keeping current activity status accessible. Lock-screen/external previews SHALL omit private message content by default. Marketing subscriptions SHALL require separate opt-in. Read receipts SHALL not be public or used as evidence of participation/completion.

#### Scenario: User mutes a conversation
- **WHEN** the user disables optional notifications
- **THEN** new optional alerts stop without deleting history or blocking cancellation and support actions

### Requirement: Blocking Reporting and Moderation
Users SHALL block unwanted new contact and privately report specific messages. Scoped moderators SHALL review only authorized evidence and record decisions/appeals under the moderation policy. Reports SHALL not automatically prove misconduct; blocked users SHALL not bypass restrictions with a different thread in the same context. Essential support and rights routes SHALL remain available.

#### Scenario: Coordinator is blocked
- **WHEN** a participant blocks abusive contact from a coordinator
- **THEN** new conversational contact stops while the participant can still cancel their commitment or contact independent support

### Requirement: Retention and Accessible Clients
Conversations SHALL have disclosed retention/deletion rules and audited restricted access, not a claim of end-to-end encryption when moderators can inspect reported content. Browser, Android and iOS SHALL expose consistent localized status and keyboard/screen-reader behavior. Departed participants SHALL lose new thread access under policy; account switches SHALL clear private conversation state.

#### Scenario: Staff assignment is revoked
- **WHEN** a former coordinator attempts to read a conversation
- **THEN** the server denies further access and active clients clear stale private content
