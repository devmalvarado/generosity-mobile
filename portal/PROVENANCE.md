# Portal source and asset provenance

The canonical static portal was adapted on 2026-10-30 from
`/Users/manuel.alvarado/Documents/Development/PoCs/generosity/portal`.
The original `index.html`, `styles.css`, `portal.js` and `assets/logo.svg` were
reviewed without modifying or deleting that PoC. The implementation remains
HTML/CSS/JavaScript; no additional UI framework was introduced.

## Brand and typography

- The owner supplied `gen2.png` and `Untitled.png` on 2026-10-31. Unmodified
  originals are retained in `../assets/brand/source/`. The transparent `gen2.png`
  was initially converted from Display P3 to sRGB for the portal wordmark.
- The current editable vector masters and generated light/dark logo, favicon,
  touch-icon and native assets are documented in
  [the brand kit](../assets/brand/README.md). They reconstruct the supplied
  artwork; they are not replacement lettering or original designer vectors.
- `../design/tokens.json` and those masters now generate portal colors,
  typography configuration and assets through the existing
  [design-system workflow](../docs/design-system.md). Edit these sources rather
  than individual exports.
- Fraunces and Outfit are requested from Google Fonts. Configured local font
  fallbacks keep content usable if those requests fail.

## Content and operational boundaries

The PoC supplied the visual direction and illustrative cause cards. These cards
are not imported catalog evidence, beneficiary records or the visitor's location.
Current public discovery reads the API separately; see
[public discovery verification](../docs/testing/public-discovery.md).
Submission, review and payment success is never inferred from clicking a button.

The owner subsequently supplied institute identity and approved adult-only
terms/privacy with `notificaciones@generosity.global` as the temporary working
contact. Current legal pages and registration acceptance supersede the PoC's
placeholder privacy anchor and contact address.

[README.md](README.md) documents local preview/testing, browser sessions and the
selective static deployment. The PHP API, migrations and private credentials have
their own deployment; they do not belong in the public static upload.

## Public app tour

On 2026-11-05, the existing `codex/visual-design-map` runtime (`design/preview/`)
was adapted into `portal/app/`. Only its HTML, CSS and three JavaScript modules
were reused; repository/specification links and task metadata were removed.
The original checkout remains unchanged. Fixture interactions remain isolated;
brand assets and colors/radii are generated from this repository's canonical
design sources. No third-party images or new runtime dependency were added.

## Published web-icon restoration — 2026-11-05

Compared the icons served by `https://generosity.global/` with local exports.
The published favicon is a transparent 192×192 PNG (SHA-256 starts
`f175327256ea`); the generated local substitute was an opaque 32×32 app-icon
export. The published favicon and 180×180 Apple touch icon are retained unchanged
in `assets/brand/source/published-*.png` and copied into the portal by the exporter.
The favicon also matches repository checkpoint `df56a60`. All five public pages
use the same versioned favicon; the generator records both source hashes to
prevent a later export from reverting them. Native app icons are unchanged.

## Public directory map — 2026-11-05

`assets/vendor/leaflet/` contains Leaflet **1.9.4**, the stable release, copied
from its official npm package with its BSD-2-Clause license and referenced images.
No framework or build step was added. Public OSM raster tiles retain visible
attribution and normal browser caching/referrer behavior; no prefetch or offline
tile downloads are performed. Browser tests replace tile responses with a fixture.
The user's location stays in page memory; OSM receives ordinary map-tile requests
(including the viewed region and connection metadata). Provider policies:
https://operations.osmfoundation.org/policies/tiles/ and
https://osmfoundation.org/wiki/Privacy_Policy.
Public OSM tiles have no availability guarantee; keep provider-capacity review in
the production release checklist if traffic grows.
