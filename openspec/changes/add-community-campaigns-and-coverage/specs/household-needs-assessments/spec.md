## Purpose
Collect voluntary community needs and support separately consented private household follow-up while preventing identifiable survey data from becoming a public map or a reward mechanism.

## ADDED Requirements

### Requirement: Approved Voluntary Fieldwork
Assessment campaigns SHALL use a scoped approved protocol, versioned questionnaire, trained assigned collectors, safety contact and private complaint route. Organizer points SHALL not bypass these controls. Participants SHALL be told this is voluntary community outreach, not an official compulsory census or a promise of aid. Declining SHALL not affect access to help; refusal SHALL not create an identifiable household record.

#### Scenario: Household declines an interview
- **WHEN** a resident refuses participation
- **THEN** no address-linked refusal or inferred need is stored, and the collector can end the visit without a penalty or lost reward

### Requirement: Separate Informed Permissions
Before recording answers, the collector SHALL explain purpose, fields, intended aggregate use, retention and withdrawal in the respondent's understood language and record consent version/time. Follow-up contact, precise location and named-center referral SHALL require separate optional permissions. No account or donation SHALL be required. Consent SHALL be revocable through a supported private channel; unapproved language/consent flows SHALL block collection.

#### Scenario: Accept statistics but decline follow-up
- **WHEN** a respondent consents to aggregate-use answers but declines follow-up and precise location
- **THEN** the system records only permitted minimal answers and coarse area, with no name, contact, address or precise coordinate required

#### Scenario: Refuse a referral but retain follow-up
- **WHEN** a household allows private follow-up but declines sharing with a proposed center
- **THEN** the case remains private and that center receives no household record

### Requirement: Minimal Structured Needs
Responses SHALL use approved service codes, quantity/unit when known, urgency, time period, coarse area and observation date, with unknown/declined values distinct from zero. Questionnaire versions SHALL retain meaning across updates. The initial flow SHALL exclude identity documents, diagnoses, audio, photographs, unrestricted sensitive narrative and direct interviews with unaccompanied minors. Structured answers SHALL not assert professional diagnoses.

#### Scenario: Need quantity is unknown
- **WHEN** a respondent identifies a food need but cannot estimate quantities
- **THEN** the category is retained with quantity unknown and is not converted into zero demand or invented meals

### Requirement: Scoped Private Cases and Referrals
Optional follow-up SHALL create a pseudonymous case with protected identifiers separate from analytical responses. Only explicitly assigned case workers SHALL access permitted fields; campaign ownership, center membership and analyst roles SHALL not grant blanket access. A referral SHALL name the consenting recipient center, purpose and minimal fields. Opening a referral SHALL not prove that help was provided or a need resolved.

#### Scenario: Follow up without broad disclosure
- **WHEN** an assigned case worker refers a consenting household to one center
- **THEN** only its assigned recipient staff receive the approved subset and unrelated centers cannot access it

#### Scenario: Resolve a case
- **WHEN** a case worker records a need as resolved
- **THEN** the history identifies date, author and whether the outcome was household-reported or independently confirmed, without automatically awarding points

### Requirement: Protected Offline Collection
Assigned collectors SHALL save encrypted device-local drafts only within a bounded authorization lease. Upload SHALL recheck consent, assignment, schema and operation identity; local submissions SHALL remain pending. Known revocation or lease expiry SHALL block reading/collection and purge sensitive caches at the configured deadline. Clients SHALL explain offline revocation limits and prevent account crossover. Sensitive drafts SHALL not enter ordinary logs, analytics or unprotected backups.

#### Scenario: Collector loses connectivity
- **WHEN** an assigned collector saves an approved consented response offline within an active lease
- **THEN** the encrypted draft survives an allowed restart, shows pending and uploads once after authorization is rechecked

#### Scenario: Revocation occurs before synchronization
- **WHEN** a queued response arrives after its consent or assignment was revoked
- **THEN** it is not incorporated into active cases or aggregates and local sensitive data is removed without silently recreating access

#### Scenario: Device stays offline beyond its lease
- **WHEN** the collection device cannot check current authorization and its local lease expires
- **THEN** collection and private reading stop and the configured purge applies, without claiming that remote revocation was received instantly

### Requirement: Quality and Revisit Provenance
Every accepted response SHALL retain survey version, consent scope, source campaign, observation time and quality state. Retries SHALL be idempotent. Explicitly linked, consented household revisits SHALL supersede matching current-period needs rather than double-count them. Potential anonymous duplicates SHALL be flagged as uncertainty, not resolved through hidden identity matching. Stale or disputed responses SHALL remain distinguishable from current observations.

#### Scenario: Revisit an existing consenting household
- **WHEN** an authorized worker updates the same need and period on the linked case
- **THEN** the current analytical contribution uses the updated observation once while private revision history remains auditable

### Requirement: Safe Aggregate Publication
Public or analytical maps, lists, charts and exports SHALL use approved coarse aggregates, never household markers or raw responses. Releases SHALL enforce minimum distinct-household cohorts, sensitive-category suppression and protection against reconstruction through filters or totals. Suppressed/unsurveyed areas SHALL not appear as zero. Missing disclosure policy or failed risk review SHALL block release. Private assigned case views SHALL remain separate from analysis.

#### Scenario: Narrow filter isolates a household
- **WHEN** a map or export filter would isolate a household or reveal a suppressed cell through subtraction
- **THEN** the release is coarsened or suppressed consistently across all views and no raw coordinate is returned

#### Scenario: Anonymous responses may be duplicates
- **WHEN** distinct-household cohort size cannot be established conservatively
- **THEN** the system suppresses or coarsens the release rather than treating every response as a different household to pass the threshold

### Requirement: Withdrawal Retention and Audit
Sensitive collection SHALL stay disabled until approved retention, deletion, access-audit and incident procedures are configured. Withdrawal/deletion SHALL remove identifiers and withdraw linked responses from future releases, revoking case/referral access and invalidating affected cached reports. Irreversibly de-identified prior releases SHALL have their limits explained. Backups SHALL expire under a documented policy without silently restoring withdrawn records.

#### Scenario: Household withdraws after a referral
- **WHEN** the supported private channel verifies and records withdrawal
- **THEN** active recipients lose system access, downstream deletion is tracked, future aggregates exclude linked responses and any already disclosed-data limits are explained

### Requirement: Accessible Non-Coercive Collection
Native assessment forms and mobile-first case views SHALL support approved Spanish/English questionnaires, readable consent, manual coarse-area selection, correction and safe pause/exit. Location permission SHALL not be mandatory. Neither completed surveys, individual need severity nor referral counts SHALL award points, unlock organizer privileges or rank residents or collectors.

#### Scenario: Collector denies device location
- **WHEN** location permission is unavailable but the collector has valid assignment and respondent consent
- **THEN** an approved coarse area can be selected manually and collection proceeds without fabricated GPS or a tracked door-to-door route
