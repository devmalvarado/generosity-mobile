# Mobile, Backend and Portal Spec Index

Planning update: 2026-11-01. This active change contains 16 capability specs:
seven extensions of the previously synchronized baseline and nine new
capabilities. They specify required behavior, not completed features. The
new planning deltas have not been archived or synchronized into main specs.

The subsequent [reference product features](../add-reference-product-features/spec-index.md)
change adds 10 separate capabilities for the image's missing functions, including
new navigation, reviews, events and external payments. The scope limits below
apply to this foundation; they do not cancel that explicitly requested follow-up.

## Confirmed User Decisions

- The first screen is the welcome phone in the supplied image: guest, sign-in and registration.
- Language follows the device by default and can be overridden in user settings.
- Backend, authentication, durable server data and multiuser synchronization are now in scope.
- Social entry includes Google, Apple and planned Facebook for Generosity web/Android/iOS only; no institute-wide SSO.
- Account deletion permanently closes access, including recovery/social entry; soft deletion is supplemented by actual personal-data removal and justified limited retention. The institute's supplied fiscal identity/address and approved support/legal/privacy addresses are recorded in the [Spanish terms draft](../../../docs/legal/terms-and-conditions.es.md); institute personnel will handle these channels, but mailbox creation and operational checks remain pending.
- Mobile contact is private and progressive, verified only for explicitly approved action requirements; the action list and delivery provider remain open decisions.
- Users submit/cancel their own offers; authorized center staff confirm/fulfill them through the backend.
- Personal helping activities are explicitly self-reported, not center-verified.
- The existing landing also becomes a mobile-first web portal for centers to publish needs, using the same backend as the apps.
- Native screens should look closer to the supplied image through visual design only; preserve existing flows and defer new navigation, favorites, achievements and streaks.

## Feature Coverage

| Requested feature | Capability specs | Task references |
| --- | --- | --- |
| Device language and manual settings | [app-localization](specs/app-localization/spec.md): system/en/es, ordered device languages, persistence and live updates. | 4.3, 4.6-4.7 |
| Welcome matching the image | [mobile-welcome](specs/mobile-welcome/spec.md): visual hierarchy, guest/account routes, returning state, accessibility and legal destinations. | 14.1-14.4 |
| Existing screens matching the image | [mobile-visual-alignment](specs/mobile-visual-alignment/spec.md): shared visual language, map-led composition, center/need hierarchy, profile/impact styling and accessible states without new functions. | 17.1-17.10 |
| Centralized native/web design system | [mobile-visual-alignment](specs/mobile-visual-alignment/spec.md): one token source, SVG masters, generated platform themes/assets, scalable typography and contrast/drift checks. [Technical design](design.md#shared-design-system-implementation--2026-11-01) and [executed simulator checks](../../../docs/testing/design-system.md). | 17.11-17.13 |
| iOS/Android parity | [mobile-platform-architecture](specs/mobile-platform-architecture/spec.md): shared state, consistent outcomes and server trust boundary. | 5.4, 7.6-7.7, 12.3-12.4 |
| Preserve data after closing | [local-data-persistence](specs/local-data-persistence/spec.md): isolated accounts, restart recovery, idempotent offline outbox and conflict handling. | 7.11-7.12, 12.1-12.4 |
| Map and location | [kindness-center-discovery](specs/kindness-center-discovery/spec.md): real maps, permitted GPS, manual origin, distances and denial/failure fallback. | 5.2, 5.6-5.9 |
| Registration and sign-in | [user-authentication](specs/user-authentication/spec.md): Generosity-only Google/Apple/Facebook entry, missing-email verification, explicit linking, secure web/native sessions, expiry, recovery and authorization. | 7.8-7.10, 11.4, 18.1-18.10 |
| Permanent account deletion and legal entry | [user-authentication](specs/user-authentication/spec.md): irrevocable access closure, in-app/web requests, data purge/retention, provider revocation, client cleanup and versioned terms acceptance. | 20.1-20.8 |
| Progressive mobile contact | [user-authentication](specs/user-authentication/spec.md) and [user-contribution-profile](specs/user-contribution-profile/spec.md): private international numbers, possession proof, action-specific server enforcement and safe replacement/removal; no blanket phone requirement. | 19.1-19.7 |
| Complete contribution forms | [user-contribution-profile](specs/user-contribution-profile/spec.md) and [kindness-center-needs](specs/kindness-center-needs/spec.md): entered quantities, time, availability, notes, validation and server availability. | 7.4, 7.7, 7.13-7.14, 7.16 |
| Lifecycle, history and impact | [contribution-lifecycle](specs/contribution-lifecycle/spec.md) and [user-contribution-profile](specs/user-contribution-profile/spec.md): role-authorized transitions, cancellation, units, provenance and no duplicate impact. | 7.2, 7.6, 7.16, 13.1-13.6 |
| Real catalog | [imported-center-catalog](specs/imported-center-catalog/spec.md): validated JSON, atomic import/refresh, backend revisions and last-known-good cache. | 3.5, 11.5, 12.3-12.4 |
| Notifications and reminders | [location-notifications](specs/location-notifications/spec.md): scheduling/edit/delete, delivery, interests, cooldown, permissions and backend opt-out acknowledgement. | 8.3-8.13, 12.5-12.6 |
| Visits and contact | [center-verification-signals](specs/center-verification-signals/spec.md) and [kindness-center-discovery](specs/kindness-center-discovery/spec.md): durable visits, optional coordinates, guest ownership and safe phone/email/web actions. | 5.10-5.11, 9.1, 9.5-9.6, 11.6 |
| Real dates and timezones | [app-time](specs/app-time/spec.md): server time, inclusive UTC date-only expiry, real clocks and unambiguous reminder instants. | 6.6, 8.3, 8.7, 13.1 |
| Backend and multiuser sync | [backend-platform](specs/backend-platform/spec.md): durable API, contracts, role provisioning, cursor sync, notification events and operational recovery. | 11.1-11.8, 12.1-12.6 |
| Landing and mobile-first center portal | [center-web-portal](specs/center-web-portal/spec.md): staff access, private drafts, publish/edit/close, contribution actions, 320-pixel layouts, accessible localized forms and truthful public states. [imported-center-catalog](specs/imported-center-catalog/spec.md) prevents imports overwriting staff-managed data. | 16.1-16.12, 15.1 |

## Implementation Order

1. Connect iOS to shared state and establish durable local models/storage. Keep current anonymous browsing working.
2. Build backend contracts, identity and transactional catalog/contribution storage alongside that persistence work.
3. Consolidate and adapt the landing mobile-first, then connect center sign-in and need publishing to the established backend. Public presentation work can run alongside backend work.
4. Complete portal publication -> mobile discovery -> offer -> portal-authorized transition -> mobile history, including offline/restart/conflict tests.
5. Land device-language settings, welcome and reference-aligned styling of existing native screens with visual/accessibility acceptance. Styling can proceed against explicit fixtures while functional integrations remain separately tracked.
6. Complete real maps/location/contact and visit queues, then reminder/nearby/urgent delivery.

Task IDs preserve the original audit history and do not imply that group 11
waits for all work in groups 1-10. Each task records its verification; the final
cross-system group supplements, rather than replaces, per-feature tests.

## Explicit Limits

No official organization verification, real payment
processing, tax receipts, review/rating system, favorites, global needs-feed
redesign, events, rewards/streak engine or automated third-party harvesting
is introduced. The scoped center workspace does not include public staff
self-enrollment, general administration, moderation, media uploads or a new
web donor transaction flow. Operator role provisioning remains controlled;
staff publication and contribution actions use the same API. Provider/runtime selections and
approved assets/legal URLs are implementation/release prerequisites, not
features assumed to exist.

See [design.md](design.md) for chosen boundaries and [tasks.md](tasks.md) for
progress. Keep this change active until implementation and evidence are complete.
