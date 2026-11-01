# Shared design configuration

Generosity's native themes, public portal and hosted account pages use
[`design/tokens.json`](../design/tokens.json). There is no runtime theme service
or additional UI framework. Android keeps Compose/Material, iOS keeps SwiftUI
and Dynamic Type, and the web keeps its existing CSS components.

## Edit once, generate, verify

| Change | Authoritative source |
| --- | --- |
| Primary actions, text, surfaces, errors and category colors | `colors` in `design/tokens.json`; each pair is light/dark |
| Logo color, light/dark lettering and app-icon background | `brand` in the same file |
| Landing's editorial neutral colors and focus/disabled states | `web` in the same file |
| Body/heading font families and Android type scale | `typography` in the same file |
| Common spacing, corners, logo bounds and minimum touch targets | `spacing`, `radii`, `sizes` in the same file |
| Symbol and wordmark outlines | `assets/brand/master/symbol.svg` and `wordmark.svg` |

The action accent is shared by native themes, landing buttons and hosted
registration/recovery. It is deliberately darker than the logo so white button
text remains readable (5.06:1). Web actions now use that same accessible accent.
The landing retains its editorial fonts and warm neutral surfaces. Provider
buttons retain Google's and Apple's own artwork, typography and brand colors.

One-time asset-authoring environment (dependencies were already used by the
brand exporter):

```sh
python3 -m venv /tmp/generosity-design-venv
/tmp/generosity-design-venv/bin/pip install -r scripts/brand-requirements.txt
```

After editing the configuration or a master logo, from the repository root:

```sh
/tmp/generosity-design-venv/bin/python scripts/generate-design-system.py
python3 scripts/generate-design-system.py --check
```

The generator writes Kotlin/Swift tokens, portal/account CSS, and (when the
brand inputs change) native vector resources, launcher/store icons, SVG/PNG/ICO
exports, the downloadable brand kit and the portal's logo/favicon files.
`design/generated.json` records the brand inputs and output hashes. Do not edit
generated files manually. `--check` needs only Python's standard library and
does not modify files. Android pre-build, Xcode pre-build, portal pre-test and
the publishing workflows reject stale resources. CI also checks changes.

Use a system family such as `sans-serif`/`serif` on Android; its Material text
roles and system font scaling are preserved. For iOS, `system` preserves semantic
text roles and Dynamic Type; `design` supports default, rounded, serif or
monospaced. A custom iOS family uses its registered PostScript name and scales
relative to each native text role. New bundled fonts still need licensed files
and platform registration once; configuration cannot install an absent font.
Web font stacks and the landing Google Fonts stylesheet URL are configured
together. Hosted account pages load only same-origin fonts and default to the
system family, preserving their restrictive security policy.

Master SVGs are flat paths, with a `viewBox` starting at `0 0` and
`data-color="brand"` or `data-color="text"` on each path. Outlined lettering is
independent of the UI typeface. Flatten groups/transforms on export; paths may
use `translate(x y) scale(x y)`. Unsupported structures fail with an error;
there is no silent raster substitution. The originals and historical tracing
geometry remain under `assets/brand/source` and `geometry.json`. Retracing the
original supplied PNG is a separate explicit `--trace-source` operation in
`generate-brand-assets.py`; ordinary generation preserves the master outlines.

## Checks and release scope

```sh
/tmp/generosity-design-venv/bin/python scripts/test-design-system.py
python3 scripts/generate-design-system.py --check
node scripts/check-hosted-design.cjs  # Local PHP API at 127.0.0.1:8088; portal dependencies installed
cd portal && npm test
```

Checks exercise a changed action color across all four generated surfaces,
font/radius changes, invalid configuration, text contrast, minimum touch sizes,
stale CSS and modified logo detection. With the authoring dependencies installed,
a fifth test changes the logo color in a temporary checkout and checks native/web
exports and deterministic regeneration. CI runs the four dependency-free checks. Browser checks assert actual computed
button color/font/radius and run the existing mobile layouts, keyboard and
200-percent text scenarios. Rebuild both native apps after design changes;
publish portal/API assets through their existing separate workflows. There is
no remote appearance change for an already installed mobile release.

Map tiles and platform-owned UI (keyboard, system dialogs, OS tab/navigation
chrome) retain their platform rendering. Google's marker API accepts hue rather
than an exact RGB color; our marker hues now derive from the theme. Geometry
constraints such as map height and responsive breakpoints stay in their layouts.

Executed checks are recorded in [design verification](testing/design-system.md)
and foundation tasks 17.11–17.13. The
broader visual/device/VoiceOver acceptance in task 17.10 remains separate.
