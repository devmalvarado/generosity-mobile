# Design consolidation verification — 2026-11-01

Scope: shared theme configuration and SVG master export for Android, iOS,
the portal and PHP hosted account/legal pages. Validation was local; this record
does not certify production deployment or provider-account configuration.

| Check | Result |
| --- | --- |
| Generator unit/integration checks in the brand virtualenv | 5 passed: changed shared color, typography/radius propagation, invalid/low-contrast configuration, stale output, full recolored native/web export with deterministic regeneration |
| `generate-design-system.py --check` | Passed; source inputs and generated resource hashes agree |
| Android debug APK and instrumentation compilation | Passed using the installed JDK/SDK and cached Gradle dependencies |
| Android JVM tests | 5 passed |
| Android emulator UI checks | 4 passed on `Generosity_API_36`: welcome and profile in ES/EN with enlarged text, guest profile and discovery/detail navigation |
| iOS simulator build | Passed with Xcode |
| iOS UI checks | 3 passed on iPhone 16 Pro / iOS 18.6: bilingual welcome, guest profile and Spanish visual tour through map/list/detail/needs/profile/settings/keyboard/empty state |
| Portal Chromium/WebKit | 69 passed; one existing duplicate screenshot case intentionally skipped. Includes 320–1280px layouts, keyboard navigation, ES/EN, 200% text, forms and theme-variable rendering |
| PHP `SocialAuthTest` | 14 passed, 201 assertions, isolated local test database |
| Hosted account/legal browser check | Chromium and WebKit passed in ES/EN; theme CSS loads under CSP, provider styling is preserved and account entry fits 320px at 200% text |
| OpenSpec strict foundation validation | Passed |

The small-screen account check found overflowing headings/provider labels with
200% text. Common spacing/radii now retain CSS-pixel dimensions as native
spacing does; text remains scalable. Account text can wrap, grid columns shrink
and provider labels do not force horizontal overflow. Screenshots from iOS
welcome/profile and hosted account entry were inspected. Portal captures remain
in `portal/screenshots/`.

Reproduce generation checks using the [design workflow](../design-system.md).
`node scripts/check-hosted-design.cjs` requires the existing local Compose API
at `127.0.0.1:8088` and the portal's installed Playwright dependencies. It only
reads hosted pages; it neither submits credentials nor sends mail.

Physical devices, all screen-reader journeys, newly supplied/bundled font
files, real Google map tiles and production cache/deployment behavior were not
revalidated. The larger visual acceptance matrix remains open in foundation
task 17.10. Native releases still need rebuilding and distribution; web assets
need their ordinary separate portal/API publication.
