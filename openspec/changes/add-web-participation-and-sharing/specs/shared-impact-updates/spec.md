## Purpose
Help communities share credible collective progress and return to relevant opportunities while measuring completed helping rather than rewarding exposure or downloads.

## ADDED Requirements

### Requirement: Truthful Collective Progress
Public progress SHALL separate requested, committed, reported and validated completed amounts with compatible units, period and update time. Cancelled/refunded/corrected evidence SHALL reconcile without double counting. Unknown values SHALL not become zero or fabricated progress. Personal helping histories and household response counts SHALL not be exposed as public achievement totals.

#### Scenario: Offers fill the requested quantity
- **WHEN** enough goods have been promised but not delivered
- **THEN** progress shows committed quantity separately and does not claim the need has been fulfilled

### Requirement: Consented Outcome Publications
Authorized organizers/centers SHALL draft and publish attributed outcome updates with visible provenance and moderation/reporting access. Identifiable names, photographs or testimonials SHALL require specific publication permission; household records and sensitive beneficiary details SHALL never populate updates automatically. Withdrawal or invalidated evidence SHALL trigger correction or removal of hosted derivatives.

#### Scenario: Beneficiary declines publicity
- **WHEN** a beneficiary declines permission to appear in an update
- **THEN** the update can describe safe aggregate results without identifying them or reducing their access to assistance

### Requirement: Opt-In Continuity
Users SHALL choose whether to follow public center/campaign updates and receive available email/inbox/push notices. Channels SHALL require their own consent and permissions, support unsubscribe, and avoid duplicate sends. Following SHALL not imply enrollment, location tracking or public membership. Relevant next opportunities SHALL remain optional and use current availability.

#### Scenario: Browser user has no push support
- **WHEN** that user opts into supported email updates
- **THEN** confirmation and unsubscribe work without installing an app or enabling another channel automatically

### Requirement: Privacy-Conscious Funnel Metrics
Authorized operators SHALL see aggregate opportunity visits, acknowledged commitments, completed validated activities and return participation with defined periods, deduplication and attribution limits. Optional analytics SHALL respect consent, retention and suppression. Share-sheet opens SHALL not be counted as delivered invitations, and self-reported outcomes SHALL not be labeled validated. No metrics SHALL award points or reveal recipient identities.

#### Scenario: Completion webhook is replayed
- **WHEN** the same canonical completion is processed repeatedly
- **THEN** the completed-help metric changes once, independently of visit counts or invitation-channel claims

### Requirement: Accessible Shareable Summaries
Web and native clients SHALL show consistent localized progress and let users explicitly share a sanitized hosted update or accessible image/text summary. Summaries SHALL identify source/date and reported versus validated status. Corrected hosted content SHALL resolve to current status; users SHALL be warned that downloaded images and external posts cannot be remotely recalled.

#### Scenario: Old result graphic is opened after correction
- **WHEN** someone follows its source link after evidence was corrected
- **THEN** the hosted result shows current figures and correction context instead of repeating the obsolete claim
