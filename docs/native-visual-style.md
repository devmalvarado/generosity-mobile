# Native visual style

Implemented 2026-10-31 for the existing Android Compose and iOS SwiftUI
Map/List/Profile/Settings destinations. Reference: [user concept](design-reference.md).
Implementation uses native controls, typography and a small palette/helper file
per platform; it adds no design-system dependency or business model.

Update 2026-11-01: these helpers now consume generated values from
`design/tokens.json`, also used by the portal and account pages. Logos originate
in `assets/brand/master/`. See [shared design configuration](design-system.md)
for editing, font support, generation and consistency checks. The historical
implementation notes below do not describe the current backend feature status.

## Assets and hierarchy

The initial landing-logo approximation has been superseded by the user's
`gen2.png` and `Untitled.png`, vectorized on 2026-10-31. The supplied symbol
and lettering are now curves, with transparent backgrounds and a white-letter
dark variant. Display P3 to sRGB conversion yields brand orange `#F1562C`.
Android uses vector drawables; iOS uses SVG asset-catalog images. Both headers
expose the accessible name “Generosity”; native launch icons use the new symbol.
Source provenance, exports and regeneration are in [the brand kit](../assets/brand/README.md).
This records supplied asset provenance, not an independent trademark/license review. Android's
simple stroke icons are authored vector drawables; iOS uses SF Symbols.
The reference raster is retained for comparison, not cropped into app controls.

- Brand/search/categories precede the map. The map uses 52% of the available
  content height, bounded to 200–480 native units. Selected-center preview
  sits below the map, leaving native provider attribution unobscured.
- Cards are reserved for center summaries, account identity and compact metrics.
  Center detail is an unframed surface: identity, address, description, current
  needs, existing help/visit actions and verification information.
- Every need has a textual priority; selected filters have a checkmark/selected
  accessibility state. Category colors and icons are supplementary cues.
- Default spacing: 4/8 within labels, 12 between icons/content, 16 page/detail
  padding, 20 between profile sections. Corners: 10–12. Decorative tiles:
  40–52. Action targets use native 48dp Android / 44pt iOS minimums.
- Android uses Material typography; iOS uses Dynamic Type text styles.
  Categories wrap at accessibility text sizes. Android switches its four
  destinations to two rows at large text sizes and hides them while typing.
  Text, details and forms scroll;
  platform safe areas and the keyboard remain respected.

## Colors and measured text contrast

Ratios below use WCAG relative luminance on the actual opaque text/surface
pairs; all exceed the [4.5:1 normal-text criterion](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum).
They are not a claim of full accessibility conformance or map-tile contrast.

| Role | Light foreground / background | Ratio | Dark foreground / background | Ratio |
| --- | --- | --- | --- | --- |
| Primary action | `#FFFFFF` / `#C6401D` | 5.06 | `#4C1500` / `#FFB197` | 8.47 |
| Body | `#191C22` / `#FAF8F5` | 16.10 | `#F7F5F2` / `#17191C` | 16.19 |
| Secondary on card | `#59616C` / `#FFFFFF` | 6.27 | `#B8BCC3` / `#222529` | 8.07 |
| Accent text on card | `#C6401D` / `#FFFFFF` | 5.06 | `#FFB197` / `#222529` | 8.78 |
| Urgency/error on iOS card | `#B3261E` / `#FFFFFF` | 6.54 | `#FFB4AB` / `#222529` | 9.06 |

Android urgency uses Material's native accessible error pair. Cards are white
in light mode and `#222529` in dark mode. Separators are `#E4E2DF`/`#444950`;
they do not carry meaning by themselves. Category icons use purple, green or
brown on tinted backgrounds and are decorative alongside readable names.
The darker action orange intentionally differs from the reference's bright
orange so white button copy remains readable.

## Data and deliberate omissions

The current shared catalog/clock are demonstration data. No center photography,
ratings, distance, open status, avatar, points balance, streak or achievements
are fabricated. Points are explicitly unavailable. Only existing center/need
counts are shown, with the local confirmed/completed basis stated; an entry
count is never presented as hours or currency. Personal-help history is marked
self-reported. These are not authoritative backend results.

The welcome screen, new Needs/Impact/+ navigation, favorites, real photo/hour
data, gamification and complete contribution forms remain separate specs.
Android keeps its existing offer actions; iOS needs remain read-only. The
Google Maps adapter is still replaceable; no key is configured, so Android
map-unavailable captures are fallback evidence only. Apple MapKit remains native.

See [native visual verification](testing/native-visual-verification.md) for
executed checks, captures and remaining acceptance work.
