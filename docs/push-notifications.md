# Urgent-need notifications

Implementation and deployment: 2026-11-02 (2026-11-03 UTC).
Evidence: [release checks](releases/push-delivery-2026-11-02.md).

## What is available

Publishing an urgent need, or changing a published need from normal to urgent,
creates an owned notice for verified active accounts that opted into urgent needs
and a matching center category, or explicitly selected all categories/locations.
Quantity-only edits do not send another alert. This first slice does not send
proximity, reminder, contribution-status or event notifications.

Android uses FCM. iOS has the Firebase Messaging/APNs adapter but delivery is
disabled until institutional Apple signing and an APNs credential are available.
Both apps can synchronize and read their own inbox without OS push permission.
Settings keep urgent preferences, category interests, OS permission and provider
availability separate. Signing in or opening the inbox never requests permission.
The explicit all-categories option currently includes all locations; there is no
server proximity matching or background location collection in this slice.

To receive Android alerts: install build 3 or later, sign in with a verified
account, enable urgent needs, select interests or all categories/locations, and
grant notification permission. A new eligible publication then produces an alert.
Existing needs are not retroactively broadcast when opting in.

## Data and delivery

- The existing MySQL `jobs` queue is the transactional outbox. `PublishUrgentNeed`
  is enqueued on the same database connection before the need transaction commits;
  rollback removes both the domain write and job.
- `notification_preferences` stores explicit account interests. `inbox_notices`
  has a unique account/event pair. `push_deliveries` has a unique notice/installation
  pair and snapshots the token hash to suppress stale queued deliveries on rotation.
- `push_installations` stores an encrypted FCM token, a hashed installation secret,
  platform/language/permission, and a foreign key to its issuing Sanctum session.
  Logout, password reset and account deactivation revoke that session and cascade
  device registrations. A persistent installation secret is required to rebind a
  device; a foreign caller cannot replace it by guessing its ID.
- The last registered token is also retained in the existing protected native
  storage. A cold start can upload denied permission without fetching a new FCM
  token; Android uses Keystore-backed encryption and iOS uses complete file
  protection with backup exclusion for this record.
- Each send rechecks account, verification, session expiry, current token,
  preference, need publication/expiry/capacity and notice age. Revoked permission
  is uploaded on app resume/settings changes. An already accepted OS message
  cannot be recalled, so lock-screen text is generic and tap handling checks owner.
- HTTP v1 uses RS256 service-account OAuth with only `firebase.messaging` scope.
  Requests have 3-second connect/8-second total timeouts. Send jobs have a
  20-second timeout, five attempts and 60/300/900/3600-second backoff. Expired
  registrations are disabled; permanent rejection is not endlessly retried.
- `accepted` means Firebase accepted the send, **not** displayed or read.
  Receipt does not mark an inbox item read. Network ambiguity can still duplicate
  a provider send; the same Android tag/APNs collapse ID and one foreground banner
  per notice per session limit duplicate presentation. This is not exactly-once
  delivery. The provider TTL is one hour and stale server notices stop sending
  after one day.
- The existing bounded Hostinger queue cron runs each minute. Delivery may take
  a minute plus provider/OS delay; disabled permissions, force-stop or connectivity
  can prevent display. Resume/manual refresh retrieves missed notices.
- Notices and their delivery rows expire after 90 days, using the daily scheduler.
  The pilot shows only the latest 100; `hasMore` is provided but cursor UI is not
  implemented. Contribution history is not deleted by notice retention.

FCM payloads contain opaque notice/event/need/center/account IDs and generic
Spanish/English text, never beneficiary details, notes, credentials or navigation
URLs. Firebase Analytics is not included or enabled. The published privacy notice
documents the providers, purpose, device identifiers, opt-out and retention.

## Contracts and client flow

The [OpenAPI contract](../backend/openapi.json) includes:

- `POST /api/v1/me/notification-preferences` for urgent/category interests.
- `POST /api/v1/me/push-installations/{installation}` for a mobile session's
  registration, rotation, locale and authorization status. Web sessions cannot
  register device tokens. Returns `deliveryConfigured`, independently of permission.
- `GET /api/v1/sync/account` includes the owned `notices` collection.
- `POST /api/v1/me/notifications/{notice}/read` is owned and idempotent.

`BackendStore` caches notices per account and durably queues read acknowledgements.
`GenerosityAppState.receivePush` verifies owner, refreshes current data and presents
one foreground banner. Opening a notice resolves its center from the current
catalog; a removed center fails safely. Sign-out clears private views. Android
and SwiftUI share this behavior. The initial destination is the center detail,
where current published needs are displayed, not an arbitrary payload URL.

## Firebase and Android configuration

Project: `generosity-v01`, project number `857659176249`, controlled by the institute.
Android and iOS app IDs are registered for `org.generosity.app`.

Android uses Firebase BoM 35.1.0 and Messaging, with automatic initialization off
until the user enables delivery. Place the local downloaded configuration at
`androidApp/google-services.json`; it is ignored by Git. Builds without that file
keep push unavailable and the rest of the app usable.

**Important:** the Firebase console currently exports the existing Maps key for
this Android registration. Replace `client[].api_key[].current_key` locally with
the separate **Generosity Android Firebase** key before building. Its allowlist
contains only Firebase Installations API and FCM Registration API. It is restricted
to `org.generosity.app` and the development certificate:

`71:E3:02:05:BB:33:A7:9C:39:D6:5D:01:E2:D2:97:C0:7E:65:F2:1B`

The Maps key keeps its original Maps-only restriction. A Play/release certificate
needs its own approved restriction before distributing a release build. Never
weaken the Maps restriction to make Firebase work. Firebase client keys identify
the project; they are separate from the private server credential.

## Private PHP configuration

The service account is `generosity-push@generosity-v01.iam.gserviceaccount.com`,
with only `roles/firebasecloudmessaging.admin`. Its JSON file is mode 0600 at
`private/storage/app/private/fcm-service-account.json` under the API domain,
outside `public_html`. No JSON private key belongs in Git, an APK or an iOS bundle.
The deployment packager excludes private runtime storage and deployment backups.

```dotenv
FCM_ENABLED=true
FCM_PROJECT_ID=generosity-v01
FCM_CREDENTIALS=/absolute/private/path/fcm-service-account.json
FCM_IOS_ENABLED=false
```

After changing private configuration, run `php artisan config:cache` and protect
the generated config cache with mode 0600. Use the existing queue and scheduler;
do not create a persistent worker on shared hosting. Monitor failed jobs and
aggregated delivery status without logging tokens or provider response bodies.
An emergency stop is `FCM_ENABLED=false` followed by config cache refresh; inbox
creation and catalog synchronization continue. Fix configuration before retrying
failed jobs; disabled/permanently rejected deliveries are not automatically replayed.

## Complete iOS activation

The repository pins Firebase Apple SDK 13.1.1 through Swift Package Manager.
The ignored `iosApp/GenerosityApp/GoogleService-Info.plist` identifies this Firebase
app. Messaging delegate swizzling and auto-initialization are off; `IOSPush`
registers APNs and forwards its token explicitly.

Pending prerequisites:

1. Active institutional Apple Developer membership and the `org.generosity.app`
   App ID with Push Notifications capability.
2. An APNs authentication key, Key ID and Team ID uploaded in this Firebase
   project's iOS Cloud Messaging settings, with the intended environment/topic.
3. Provisioning profile that includes `aps-environment`. Build with
   `GENEROSITY_PUSH_ENABLED=YES`, `CODE_SIGN_ENTITLEMENTS=Push.entitlements` and
   the matching `APS_ENVIRONMENT=development` or `production`.
4. Enable private `FCM_IOS_ENABLED=true`, refresh config, then test on an actual
   signed iPhone before declaring iOS delivery ready.

Default personal-team/simulator builds retain `GENEROSITY_PUSH_ENABLED=NO` and do
not attempt unsupported APNs registration. Do not turn the backend iOS flag on
just because the plist exists. Verify foreground, background, locked, terminated,
tap destination, denied/revoked permission, logout and token changes on signed
devices. OS banners and in-app notices require separate evidence.

References: [FCM HTTP v1](https://firebase.google.com/docs/cloud-messaging/send/v1-api),
[Android receipt](https://firebase.google.com/docs/cloud-messaging/android/receive-messages),
[APNs setup](https://firebase.google.com/docs/cloud-messaging/ios/get-started),
[Firebase key restrictions](https://firebase.google.com/docs/projects/api-keys).
