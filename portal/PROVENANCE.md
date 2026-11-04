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
