# Push delivery acceptance — 2026-11-02

Scope: first urgent-need notification slice on Laravel/MySQL, shared KMP state,
Android FCM and the gated iOS FCM/APNs adapter. This does not complete reminders,
proximity notifications or the entire reference inbox roadmap.

## Verified

Final checks: the full PHP suite passed 85 tests / 5,279 assertions, followed by
the updated targeted push suite (8 tests / 66 assertions, including category and
retention coverage). KMP passed 78 JVM and 81 iOS ARM64 simulator tests; Android
passed 14 unit tests. The shared cold-start revocation regression passed on both
KMP targets. OpenSpec strict validation passed all 16 current items.

- PHP tests cover atomic publication/outbox, no quantity-edit alerts, owned
  notices/read retries, denied OS permission, encrypted token/installation-secret
  binding, logout, preference/closure/expiry suppression, invalid-token retirement,
  token rotation, permission revocation, deactivation, transient retry and provider
  acceptance distinct from reading. `PushMessagingTest` checks OAuth/payload/TTL,
  generic lock-screen content, iOS gating and provider errors with HTTP fakes.
- Shared tests cover durable account-isolated cache/reads, stable installation
  identity, no stale token outbox replay, foreign-account rejection, one banner
  per notice, current-center navigation and private-view clearing.
- Android unit tests validate notification identifiers. The two
  `NotificationInboxTest` cases passed on the Android 14 Google APIs emulator:
  English/Spanish inbox counts, opening the center, read acknowledgement and
  sign-out isolation without asking for OS push permission.
- iOS native suite: 19 tests passed on iPhone 17 / iOS 27.0 simulator, including
  push target parsing. Shared iOS native tests and bridge preparation passed.
  This does not prove APNs delivery or physical-device notification rendering.
- Localization validation passed for English/Spanish, including placeholders,
  plurals and UTF-8/NFC. The release-packager regression excludes private FCM
  credentials and deployment backups.

## Actual Firebase delivery

`PushDeliveryTest.receivesForegroundEventAndBackgroundTrayAlert` is opt-in through
instrumentation argument `livePush=true`. It was run against the published PHP
API with one isolated, verified DEMO account and the Android 14 Google APIs
emulator (`Generosity_Google_API_34`), using build 3 and the real Firebase SDK.

The first attempt failed with `FIS_AUTH_ERROR`: Firebase's downloaded Android
configuration reused the Maps-only key. A separate Android/package/certificate
restricted key for Firebase Installations and FCM Registration fixed registration;
Maps permissions were not expanded. The corrected run passed **1/1**, in 23 seconds.

Two real API publications under `demo-generosity-20261102` produced two owned
inbox items and two provider-accepted deliveries:

- Foreground need `2a5c2f7d-827a-49bb-b938-505c33344d6e`: the native messaging
  service received the event; the OS tray remained empty.
- Background need `76ac2c20-205c-4913-9f0c-f68a15d09d60`: the emulator's
  NotificationManager reported the alert on `urgent_needs`.

The delivery test never logs the device token. It deletes its temporary token
file and clears its OS notifications. Both DEMO needs were closed through the
API, then the isolated account was logically deactivated, revoking its access
token and installation. The existing owner DEMO need and original catalog were
preserved. A partial cleanup PUT was initially rejected by validation; sending
the complete current need document successfully completed cleanup.

## Deployment and remaining gates

The backend additive migration, controllers/jobs, private configuration, OpenAPI
and privacy text were published after a private database/file/environment backup
at `private/.deployments/push-20261103T0200Z/`. The FCM service-account file and
cached configuration are mode 0600. Android sending is enabled; iOS is disabled.
Rollback can disable FCM and restore backed-up code/configuration; keep the
additive inbox tables rather than deleting notification history.

Still unverified: physical Android locked/terminated delivery and tapping its OS
alert; real APNs delivery on any iPhone; cross-device read-state UI acceptance.
The physical Android gate remains even though actual Firebase delivery worked
in the emulator. The iOS gate requires paid institutional signing and an APNs
key/profile. Do not archive either OpenSpec change based on this slice.

See [configuration and operating limits](../push-notifications.md).

The local Android handoff is `build/releases/Generosity-0.1.0-build3.apk`,
14,646,604 bytes, development signature verified with `apksigner`. SHA-256:
`144cd67b7fc8070b7960f5068829de02a28c3756d4e044c278de0d6926ecf2be`.
The APK and its checksum are local build outputs, excluded from Git. This build
uses the development certificate allowed by the Maps/Firebase keys; it is not
a Play Store release.
