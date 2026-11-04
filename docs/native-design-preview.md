# Native design preview

The 34 routes committed on `codex/visual-design-map` (`5a5bd63`) are rebuilt as **SwiftUI and Jetpack Compose**, with shared Kotlin fixture/session logic. No WebView, bundled HTML or external prototype site is used.

The design area now uses the existing mobile backend connection for supported destinations. Unsupported destinations remain session-only design fixtures. The screen inventory labels each destination **Connected / Conectado** or **Design / Diseño**; it does not imply that every roadmap feature is implemented.

## Connected destinations

| Destination | Existing integration reused |
| --- | --- |
| Map, list, filters, center | `GET /api/v1/sync/catalog`, shared catalog cache/search/category filtering, Google Maps/MapKit adapters, platform address search/location and contact actions |
| Needs | Current needs across the visible catalog, using the same expiry/priority policy as center detail |
| Offer, help chooser, personal help | Existing validated goods/time form, `POST /contributions`, durable account-bound outbox and idempotent retry |
| History, impact | `/sync/account`, server contributions/statuses and existing impact calculation; personal completion and cancellation preserve server authorization/version checks |
| Inbox | Existing account notification refresh, mark-read/open-center flow; no fabricated unread messages |
| Profile, welcome | Existing account session and browser-based sign-in/registration handoff |
| Settings | Persisted language and notification preferences, existing OS permission controls |

The same `GenerosityAppState` / `GenerosityStore` instance backs old and new layouts. Real screens never read fixture IDs, quantities, balances, profiles or preferences. A failed request shows the existing sync error and available cache; it never replaces live records with fictional ones. Mock saved-center rows cannot open an unrelated real center. Unsupported payment, phone, deletion, event, campaign and staff previews keep their previous isolation and restrictions. Staff/onboarding services available to the web portal have **not** been given native forms by this change.

`preview.*` controls access to layouts; contribution business flags, authentication and server authorization still apply separately. The connected map exposes only filters supported by the mobile model (category/search/location); fictional resource-funding controls are not sent to the server. History and impact are separate views of authoritative state, not demo totals.

Public read-only checks on 2026-11-04 returned 200 for health, centers and needs. `/api/v1/features`, `/campaigns`, `/occurrences` and `/meals` returned 404 on the production host. Consequently ordinary production navigation still requires deployment/activation of the feature API. No permission is inferred from a missing endpoint, and this change does not deploy it.

## Controls and coverage

All controls are off by default. In `/admin/features`, an allowlisted verified operator can select Off, Test accounts only (verified numeric user IDs), or Everyone. Prefer targeted test accounts when validating. Reasons, optimistic versions and audit records use the existing feature-control flow.

| Backend key | Native routes |
| --- | --- |
| `preview.discovery` | map, needs, center, saved, reviews, welcome |
| `preview.contributions` | offer, history; help chooser and personal-help form |
| `preview.impact` | impact, achievements, regions; certificate view |
| `preview.community` | events, event, campaign, meals, food; organizer eligibility view |
| `preview.assistance` | referral, case, assessment, coverage |
| `preview.organizations` | organization, businesses, funding |
| `preview.communication` | inbox, messages, share, assisted |
| `preview.operations` | staff, onboarding, operations; need editor |
| `preview.account` | profile, settings; phone and sample deletion confirmations |
| `preview.payments` | donate, tax (checkout and issuance always disabled) |

When any group is enabled for the current account, **Explore design preview / Explorar diseño de prueba** appears in the app. The inventory shows only enabled routes. Cross-group navigation requires its target flag. Connected discovery also requires the contributions preview group before showing enabled offer controls; domain contribution permissions remain authoritative. Connected welcome uses the real registration/sign-in flow. Enabling all groups makes the entire 34-route tour available.

## Remaining fixture interactions

- Shared search/category/funding filters, pin/list selection, back navigation and favorites.
- Validated positive-quantity/date offers, history, cancellation, and sample center confirmation/receipt. Offers never add real impact points.
- Event join/cancel, waitlist preview, meal slots, campaign ranking opt-in/out, organizational context and sample message blocking/read state.
- Consent-gated referrals, assessments and assisted participation; optional private follow-up needs its additional consent.
- Mock center registration checks coordinates and HTTPS links. Operations and staff previews confer no server permissions.
- Payment/tax buttons are disabled; phone sends no SMS; certificates are invalid samples; deletion clears sample state only. Sharing/contact/map actions do not dispatch public links or contact fictional destinations.

For unsupported routes and the isolated harness, all sample data stays in `PreviewSession`. It has **no repository, account, connection, storage, push, provider or platform permission dependency**. Closing the preview discards it. Effective-access changes clear session data; account changes close/recreate the area. Features follow the existing account-aware refresh/resume path, and a failed refresh cannot restore cached tester grants.

## Design implementation

`shared/.../preview/PreviewSession.kt` owns routes, bilingual copy, fixtures and native presentation items. `NativePreview.swift` and `NativePreview.kt` render native forms, cards, menus, switches, progress bars and an illustrative Canvas map. Both reuse the centralized color, type, spacing, radius and SVG-derived brand resources. The isolated mock harness retains its illustrative map. Connected design uses the existing Google/Apple map adapters and real permission-aware location flow.

Device light/dark appearance and scalable fonts are inherited. Forms and inventories scroll, map pins have names and minimum native touch sizes, and the selected center remains consistent across pin, list and detail. No new runtime dependency was added.

Navigation uses SwiftUI `TabView` with system `NavigationStack` toolbars (Liquid Glass on supported iOS versions, system fallback on earlier releases). Android uses Material 3 `TopAppBar`, `NavigationBar` and `NavigationBarItem` with the existing brand icons and theme. The inventory lives in the top toolbar. Primary tabs and nested-route selection come from the shared session and respect effective flags; groups with no primary destination omit the bottom bar.

The map header uses a compact, centered version of the same wordmark, with list/filter actions on the left and notifications/inventory on the right. Search is a floating capsule over the map: native Liquid Glass on supported iOS versions (material fallback), and an elevated Material 3 surface on Android. The location arrow requests real location in connected mode and uses the simulated origin only in the isolated fixture harness. The notifications button respects its group and opens the account inbox in connected mode. The close X is removed; `Exit design preview` is in the inventory when a dismissal action exists, and is absent from the standalone iOS test harness. Default wordmark dimensions on other screens are unchanged.

## Activation and test harness

1. Deploy the migration `2026_11_03_000002_add_native_preview_flags.php` and API registry together after an explicit deployment request.
2. Distribute a new native build (both production and preview variants contain the gated design area).
3. Enable selected groups for verified test account IDs; refresh/reopen the app and use its design entry.
4. Disable the groups to remove the entry on the next effective-feature refresh.

No hosting change is made by the native test harness. In an **iOS DEBUG build only**, launch with `--native-preview` to use an isolated all-groups fixture without credentials or a server. Release builds ignore it. Android instrumentation directly constructs `NativePreview` with fixture flags. For manual exploration, Android debug builds also include `NativePreviewActivity`, which stays open without instrumentation and has no account or backend connection. Neither mechanism is a production feature-access fallback; the activity and its manifest entry are absent from release builds.

After installing the preview debug APK, open the isolated Android design in Spanish:

```sh
adb shell am start -n org.generosity.app.preview/org.generosity.app.NativePreviewActivity --es language es
```

Omit the `language` extra to follow the device language. Use the screen inventory to exit.

See [test evidence](testing/native-design-preview.md) and [OpenSpec tasks](../openspec/changes/add-native-design-preview/tasks.md). Activation and real service implementation remain separate work.

For manual **connected** inspection in a DEBUG build, use `--connected-design` on iOS or:

```sh
adb shell am start -n org.generosity.app/org.generosity.app.ConnectedDesignActivity
```

These entries use the real connection/account/cache and bypass only design-navigation visibility locally; they do not grant contribution permissions or backend roles. The Android activity is absent from release builds and iOS ignores its argument in Release. Use the production debug APK when checking production catalog data; the preview variant retains its separate environment. Existing `--native-preview` / `NativePreviewActivity` remain completely isolated. No server changes are made by opening either harness.
