#!/usr/bin/env python3
"""Generate platform themes and brand resources from the repository design sources."""

import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "design/tokens.json"
MANIFEST = ROOT / "design/generated.json"


def load_tokens(path=SOURCE):
    data = json.loads(path.read_text())
    for values in data["colors"].values():
        if len(values) != 2:
            raise ValueError("Each native color needs light and dark values")
    for color in [c for pair in data["colors"].values() for c in pair] + list(data["brand"].values()) + list(data["web"].values()):
        if not isinstance(color, str) or not re.fullmatch(r"#[0-9A-Fa-f]{6}", color):
            raise ValueError(f"Invalid six-digit color: {color!r}")
    for foreground, background in [("onAccent", "accent"), ("text", "surface"), ("text", "background"), ("secondary", "surface"), ("onError", "error")]:
        for mode in range(2):
            if contrast(data["colors"][foreground][mode], data["colors"][background][mode]) < 4.5:
                raise ValueError(f"Insufficient text contrast: {foreground}/{background}, appearance {mode}")
    for group in ["spacing", "radii", "sizes"]:
        if any(not isinstance(n, (int, float)) or not 0 < n < 1000 for n in data[group].values()):
            raise ValueError(f"Invalid {group}")
    for key, minimum in [("androidTouch", 48), ("iosTouch", 44), ("webTouch", 44)]:
        if data["sizes"][key] < minimum:
            raise ValueError(f"{key} must be at least {minimum}")
    typography = data["typography"]
    if typography["ios"]["design"] not in ["default", "rounded", "serif", "monospaced"]:
        raise ValueError("Unsupported iOS system font design")
    if not 0.8 <= typography["android"]["scale"] <= 2:
        raise ValueError("Android type scale must be between 0.8 and 2")
    for platform in ["ios", "android"]:
        for role in ["body", "heading"]:
            if not re.fullmatch(r"[A-Za-z0-9 ._-]+", typography[platform][role]):
                raise ValueError("Use a system family or a registered font name")
    for value in [typography["web"][key] for key in ["body", "heading", "account"]]:
        if any(c in value for c in "\n\r{};<>"):
            raise ValueError("Invalid CSS font configuration")
    url = typography["web"]["stylesheet"]
    if url and not re.fullmatch(r"https://fonts\.googleapis\.com/[A-Za-z0-9?=&:;.,@|%+/_-]+", url):
        raise ValueError("Use a Google Fonts stylesheet URL or an empty string for local/system fonts")
    return data


def kebab(name):
    return re.sub(r"[A-Z]", lambda m: "-" + m[0].lower(), name)


def contrast(first, second):
    def luminance(color):
        channels = [int(color[i:i + 2], 16) / 255 for i in (1, 3, 5)]
        linear = [v / 12.92 if v <= 0.04045 else ((v + 0.055) / 1.055) ** 2.4 for v in channels]
        return sum(c * weight for c, weight in zip(linear, (0.2126, 0.7152, 0.0722)))
    low, high = sorted([luminance(first), luminance(second)])
    return (high + 0.05) / (low + 0.05)


def rgb(hex_color):
    return ", ".join(str(int(hex_color[i:i + 2], 16)) for i in (1, 3, 5))


def render(data):
    banner = "Generated from design/tokens.json. Run scripts/generate-design-system.py; do not edit."
    android = [f"// {banner}", "package org.generosity.app.ui", "", "import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.unit.dp", "", "internal object GenColors {"]
    swift = [f"// {banner}", "import SwiftUI", "", "enum GenPalette {"]
    for name, (light, dark) in data["colors"].items():
        android.extend([f"    val {name}Light = Color(0xFF{light[1:]})", f"    val {name}Dark = Color(0xFF{dark[1:]})"])
        swift.append(f"    static let {name} = adaptiveColor(0x{light[1:]}, 0x{dark[1:]})")
    android.append("}")
    swift.append("}")
    for group, name in [("spacing", "GenSpace"), ("radii", "GenRadius"), ("sizes", "GenSize")]:
        android.append(f"\ninternal object {name} {{")
        swift.append(f"\nenum {name} {{")
        for key, value in data[group].items():
            android.append(f"    val {key} = {value}.dp")
            swift.append(f"    static let {key}: CGFloat = {value}")
        android.append("}")
        swift.append("}")
    a, i = data["typography"]["android"], data["typography"]["ios"]
    android.extend(["\ninternal object GenType {", f'    const val body = "{a["body"]}"', f'    const val heading = "{a["heading"]}"', f'    const val scale = {a["scale"]}f', "}"])
    swift.extend(["\nenum GenType {", f'    static let body = "{i["body"]}"', f'    static let heading = "{i["heading"]}"', f'    static let design: Font.Design = .{i["design"]}', "}"])
    css = [f"/* {banner} */"]
    fonts = data["typography"]["web"]
    if fonts["stylesheet"]:
        css.append(f'@import url("{fonts["stylesheet"]}");')
    css.append(":root {")
    for name, values in data["colors"].items():
        css.append(f"  --gen-{kebab(name)}: {values[0]};")
        css.append(f"  --gen-{kebab(name)}-rgb: {rgb(values[0])};")
    for name, value in data["web"].items():
        css.append(f"  --gen-web-{kebab(name)}: {value};")
        css.append(f"  --gen-web-{kebab(name)}-rgb: {rgb(value)};")
    for name in ["body", "heading", "account"]:
        css.append(f"  --gen-font-{name}: {fonts[name]};")
    for group, prefix in [("spacing", "space"), ("radii", "radius")]:
        for name, value in data[group].items():
            css.append(f"  --gen-{prefix}-{kebab(name)}: {value:g}px;")
    css.extend([f'  --gen-touch: {data["sizes"]["webTouch"]}px;', "}", ""])
    content = "\n".join(css)
    return {
        "androidApp/src/main/java/org/generosity/app/ui/DesignTokens.kt": "\n".join(android) + "\n",
        "iosApp/GenerosityApp/DesignTokens.swift": "\n".join(swift) + "\n",
        "portal/assets/design-tokens.css": content,
        # Auth intentionally keeps system fonts and does not request portal font services.
        "backend/public/auth-assets/design-tokens.css": "\n".join(line for line in css if not line.startswith("@import")),
    }


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def brand_inputs(data):
    return {"brand": data["brand"], "symbol": digest(ROOT / "assets/brand/master/symbol.svg"),
            "wordmark": digest(ROOT / "assets/brand/master/wordmark.svg"),
            "generator": digest(ROOT / "scripts/generate-brand-assets.py"),
            "readme": digest(ROOT / "assets/brand/README.md")}


def brand_paths():
    paths = []
    for folder in ["assets/brand/svg", "assets/brand/png", "assets/brand/web"]:
        paths.extend(p for p in (ROOT / folder).rglob("*") if p.is_file())
    for pattern in ["androidApp/src/main/res/drawable/generosity_*.xml", "androidApp/src/main/res/values*/brand_colors.xml",
                    "androidApp/src/main/res/mipmap-*/ic_launcher.*", "iosApp/GenerosityApp/Assets.xcassets/Generosity*/*",
                    "iosApp/GenerosityApp/Assets.xcassets/AppIcon.appiconset/*"]:
        paths.extend(ROOT.glob(pattern))
    paths.extend(ROOT / p for p in ["portal/assets/logo.png", "portal/assets/favicon.png", "portal/assets/apple-touch-icon.png"])
    paths.extend(ROOT / "assets/brand" / name for name in ["preview.svg", "preview.png", "verification.json", "generosity-brand-kit.zip"])
    return sorted(paths)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Check generated files without writing or installing anything")
    args = parser.parse_args()
    data = load_tokens()
    outputs = render(data)
    old = json.loads(MANIFEST.read_text()) if MANIFEST.exists() else {}
    dirty = [name for name, content in outputs.items() if not (ROOT / name).exists() or (ROOT / name).read_text() != content]
    brand_dirty = old.get("inputs") != brand_inputs(data) or not old.get("files")
    for name, expected in old.get("files", {}).items():
        if not (ROOT / name).is_file() or digest(ROOT / name) != expected:
            brand_dirty = True
    if args.check:
        if dirty or brand_dirty:
            raise SystemExit("Stale design output: " + ", ".join(dirty + (["brand assets"] if brand_dirty else [])) + ". Run scripts/generate-design-system.py.")
        print("Design tokens and brand assets are synchronized.")
        return
    if brand_dirty:
        subprocess.run([sys.executable, str(ROOT / "scripts/generate-brand-assets.py")], check=True)
    for name, content in outputs.items():
        path = ROOT / name
        path.parent.mkdir(parents=True, exist_ok=True)
        if not path.exists() or path.read_text() != content:
            path.write_text(content)
    MANIFEST.write_text(json.dumps({"inputs": brand_inputs(data), "files": {str(p.relative_to(ROOT)): digest(p) for p in brand_paths()}}, indent=2) + "\n")
    print("Updated Android, iOS, portal and account design resources.")


if __name__ == "__main__":
    main()
