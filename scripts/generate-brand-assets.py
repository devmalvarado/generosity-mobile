#!/usr/bin/env python3
"""Export the master vector geometry using design/tokens.json; optionally retrace the original PNG."""

from io import BytesIO
import argparse
import json
from pathlib import Path
import re
import struct
import subprocess
import tempfile
import xml.etree.ElementTree as ET
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED

from PIL import Image, ImageChops, ImageCms
import resvg_py

ROOT = Path(__file__).resolve().parents[1]
BRAND = ROOT / "assets/brand"
SVG_NS = "http://www.w3.org/2000/svg"
TEXT_DARK, TEXT_LIGHT = "#000000", "#FFFFFF"


def write(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    content = content if isinstance(content, bytes) else content.encode("utf-8")
    if not path.exists() or path.read_bytes() != content:
        path.write_bytes(content)


def json_file(path, data):
    write(path, json.dumps(data, indent=2) + "\n")


def srgb(path):
    image = Image.open(path).convert("RGBA")
    if image.info.get("icc_profile"):
        image = ImageCms.profileToProfile(image, ImageCms.ImageCmsProfile(BytesIO(image.info["icc_profile"])),
                                          ImageCms.createProfile("sRGB"), outputMode="RGBA")
    return image


def color_mask(image, orange):
    mask = Image.new("L", image.size)
    mask.putdata([255 if a >= 128 and (r > 180 and r - g > 65 and r - b > 95 if orange else max(r, g, b) < 128)
                  else 0 for r, g, b, a in image.get_flattened_data()])
    return mask


def trace(masks, colors):
    combined = masks[0]
    for mask in masks[1:]:
        combined = ImageChops.lighter(combined, mask)
    x0, y0, x1, y1 = combined.getbbox()
    bounds = (x0 - 2, y0 - 2, x1 + 2, y1 + 2)
    paths = []
    with tempfile.TemporaryDirectory(prefix="generosity-trace-") as directory:
        for mask, color in zip(masks, colors):
            source, output = Path(directory) / "mask.pgm", Path(directory) / "trace.svg"
            ImageChops.invert(mask.crop(bounds)).save(source)
            subprocess.run(["potrace", str(source), "--svg", "--flat", "--opttolerance", "0.1", "--unit", "100", "--output", str(output)], check=True)
            group = ET.parse(output).getroot().find(f"{{{SVG_NS}}}g")
            transform = re.fullmatch(r"translate\(([-\d.]+),([-\d.]+)\) scale\(([-\d.]+),([-\d.]+)\)", group.attrib["transform"])
            assert transform, "Unexpected tracer transform"
            for path in group.findall(f"{{{SVG_NS}}}path"):
                paths.append({"d": " ".join(path.attrib["d"].split()), "x": float(transform[1]), "y": float(transform[2]),
                              "sx": float(transform[3]), "sy": float(transform[4]), "color": color})
    return {"width": bounds[2] - bounds[0], "height": bounds[3] - bounds[1], "paths": paths}, combined.crop(bounds)


def path_markup(paths, text_color=TEXT_DARK, monochrome=None):
    return "\n".join(f'<path fill="{monochrome or (text_color if p["color"] == "text" else p["color"])}" '
        f'fill-rule="evenodd" transform="translate({p["x"]:g} {p["y"]:g}) scale({p["sx"]:g} {p["sy"]:g})" d="{p["d"]}"/>' for p in paths)


def svg(width, height, content, name):
    return (f'<svg xmlns="{SVG_NS}" viewBox="0 0 {width:g} {height:g}" width="{width:g}" height="{height:g}" '
            f'role="img" aria-labelledby="title"><title id="title">{name}</title>\n{content}\n</svg>\n')


def vector(geometry, name, dark=False):
    return svg(geometry["width"], geometry["height"], path_markup(geometry["paths"], TEXT_LIGHT if dark else TEXT_DARK), name)


def png(svg_text, width):
    return resvg_py.svg_to_bytes(svg_string=svg_text, width=width, skip_system_fonts=True)


def image_set(name, variants):
    folder = ROOT / f"iosApp/GenerosityApp/Assets.xcassets/{name}.imageset"
    images = []
    for dark, content in variants:
        filename = name + ("-dark" if dark else "") + ".svg"
        write(folder / filename, content)
        entry = {"filename": filename, "idiom": "universal"}
        if dark:
            entry["appearances"] = [{"appearance": "luminosity", "value": "dark"}]
        images.append(entry)
    json_file(folder / "Contents.json", {"images": images, "info": {"author": "xcode", "version": 1},
        "properties": {"preserves-vector-representation": True}})


def android_vector(geometry, size=None, offset=(0, 0), scale=1, monochrome=False, intrinsic=None):
    width, height = size or (geometry["width"], geometry["height"])
    intrinsic_width, intrinsic_height = intrinsic or (width, height)
    groups = []
    for path in geometry["paths"]:
        color = "#000000" if monochrome else "@color/generosity_wordmark_text" if path["color"] == "text" else path["color"]
        # Potrace starts each separate letter at an absolute M; relative m counters
        # stay with their letter, preserving holes while keeping paths compact.
        letters = "".join(f'<path android:fillColor="{color}" android:fillType="evenOdd" android:pathData="{part}"/>'
                          for part in re.findall(r"M[^M]+", path["d"]))
        groups.append(f'<group android:translateX="{path["x"]:g}" android:translateY="{path["y"]:g}" android:scaleX="{path["sx"]:g}" android:scaleY="{path["sy"]:g}">'
                      + letters + '</group>')
    return (f'<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="{intrinsic_width:g}dp" '
            f'android:height="{intrinsic_height:g}dp" android:viewportWidth="{width:g}" android:viewportHeight="{height:g}">\n'
            f'<group android:translateX="{offset[0]:.5f}" android:translateY="{offset[1]:.5f}" '
            f'android:scaleX="{scale:.8f}" android:scaleY="{scale:.8f}">\n' + "\n".join(groups) + "\n</group>\n</vector>\n")


def read_master(name, brand):
    root = ET.parse(BRAND / "master" / f"{name}.svg").getroot()
    x, y, width, height = map(float, root.attrib["viewBox"].split())
    if x != 0 or y != 0 or width <= 0 or height <= 0:
        raise ValueError("Master SVGs need a positive viewBox starting at 0 0")
    paths = []
    for node in root:
        if node.tag == f"{{{SVG_NS}}}title":
            continue
        if node.tag != f"{{{SVG_NS}}}path" or node.get("data-color") not in ["brand", "text"]:
            raise ValueError("Flatten the master SVG to paths with data-color=brand or text")
        transform = node.get("transform", "translate(0 0) scale(1 1)")
        match = re.fullmatch(r"translate\(([-\d.]+)[ ,]+([-\d.]+)\) scale\(([-\d.]+)[ ,]+([-\d.]+)\)", transform)
        if not match:
            raise ValueError("Flatten SVG transforms or use translate(x y) scale(x y)")
        paths.append({"d": node.attrib["d"], "x": float(match[1]), "y": float(match[2]),
                      "sx": float(match[3]), "sy": float(match[4]),
                      "color": "text" if node.get("data-color") == "text" else brand["color"]})
    if not paths:
        raise ValueError("The master SVG has no paths")
    return {"width": width, "height": height, "paths": paths}


def main():
    global TEXT_DARK, TEXT_LIGHT
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--trace-source", action="store_true", help="Re-import the original supplied PNG into the master geometry")
    args = parser.parse_args()
    brand = json.loads((ROOT / "design/tokens.json").read_text())["brand"]
    TEXT_DARK, TEXT_LIGHT = brand["textLight"], brand["textDark"]
    orange = brand["color"]
    if args.trace_source:
        source = srgb(BRAND / "source/gen2.png")
        symbol, symbol_mask = trace([color_mask(source.crop((0, 0, source.width, 355)), True)], [orange])
        lower = source.crop((0, 355, source.width, source.height))
        wordmark, wordmark_mask = trace([color_mask(lower, True), color_mask(lower, False)], [orange, "text"])
        json_file(BRAND / "geometry.json", {"orange_srgb": orange, "symbol": symbol, "wordmark": wordmark})
        # Retracing is an explicit import; ordinary regeneration never overwrites masters.
        for name, geometry in [("symbol", symbol), ("wordmark", wordmark)]:
            root = ET.fromstring(vector(geometry, "Generosity"))
            for node in root.findall(f"{{{SVG_NS}}}path"):
                node.set("data-color", "text" if node.get("fill") == TEXT_DARK else "brand")
            write(BRAND / "master" / f"{name}.svg", ET.tostring(root, encoding="unicode"))
    symbol, wordmark = read_master("symbol", brand), read_master("wordmark", brand)
    outputs = {"symbol": vector(symbol, "Generosity symbol"),
               "wordmark": vector(wordmark, "Generosity"), "wordmark-dark": vector(wordmark, "Generosity", True)}
    # Preserve the supplied stacked composition while giving all variants transparent backgrounds.
    stacked_width, stacked_height = wordmark["width"], symbol["height"] + 44 + wordmark["height"]
    for dark in [False, True]:
        content = f'<g transform="translate({(stacked_width-symbol["width"])/2:g} 0)">{path_markup(symbol["paths"])}</g>'
        content += f'<g transform="translate(0 {symbol["height"]+44:g})">{path_markup(wordmark["paths"], TEXT_LIGHT if dark else TEXT_DARK)}</g>'
        outputs["stacked-dark" if dark else "stacked"] = svg(stacked_width, stacked_height, content, "Generosity")
    for color, name in [(TEXT_DARK, "symbol-black"), (TEXT_LIGHT, "symbol-white")]:
        outputs[name] = svg(symbol["width"], symbol["height"], path_markup(symbol["paths"], monochrome=color), "Generosity symbol")
    # App-store images stay square and opaque. Platform launchers apply their own masks.
    icon_scale = 736 / symbol["height"]
    icon = svg(1024, 1024, f'<rect width="1024" height="1024" fill="{brand["iconBackground"]}"/>'
        f'<g transform="translate({(1024-symbol["width"]*icon_scale)/2:g} 144) scale({icon_scale:g})">{path_markup(symbol["paths"])}</g>', "Generosity app icon")
    outputs["app-icon"] = icon
    for name, content in outputs.items():
        write(BRAND / "svg" / (name + ".svg"), content)
        write(BRAND / "png" / (name + ".png"), png(content, 1024 if name in ["symbol", "symbol-black", "symbol-white", "app-icon"] else 2048))

    def preview_asset(name, x, y, width, height):
        root = ET.fromstring(outputs[name])
        root.attrib.update(x=str(x), y=str(y), width=str(width), height=str(height))
        return ET.tostring(root, encoding="unicode")

    preview = '<rect width="1200" height="780" fill="#F5F3F0"/>'
    preview += '<g font-family="Arial" fill="#191C22"><text x="40" y="48" font-size="28" font-weight="bold">Generosity · archivos de marca</text>'
    preview += '<text x="40" y="78" font-size="16">Vectorización de las imágenes proporcionadas · SVG / PNG / ICO</text></g>'
    preview += '<rect x="40" y="110" width="540" height="170" rx="14" fill="white"/><rect x="620" y="110" width="540" height="170" rx="14" fill="#17191C"/>'
    preview += preview_asset("wordmark", 60, 140, 500, 100) + preview_asset("wordmark-dark", 640, 140, 500, 100)
    preview += '<rect x="40" y="320" width="300" height="380" rx="14" fill="white"/><rect x="370" y="320" width="490" height="380" rx="14" fill="white"/><rect x="890" y="320" width="270" height="380" rx="14" fill="white"/>'
    preview += preview_asset("symbol", 100, 365, 180, 260) + preview_asset("stacked", 390, 370, 450, 270)
    preview += preview_asset("app-icon", 935, 380, 180, 180)
    for size, x in [(16, 943), (32, 982), (48, 1040)]:
        preview += preview_asset("app-icon", x, 600, size, size)
    preview += '<g font-family="Arial" font-size="16" fill="#59616C"><text x="60" y="265">Fondo claro · transparencia</text><text x="640" y="265" fill="#B8BCC3">Fondo oscuro · transparencia</text><text x="60" y="680">Símbolo</text><text x="390" y="680">Composición completa</text><text x="910" y="680">Aplicación y favicon</text><text x="40" y="746">Naranja sRGB: ' + orange + ' · Letras convertidas en curvas · Iconos de tienda sin transparencia</text></g>'
    preview_svg = svg(1200, 780, preview, "Generosity brand asset preview")
    write(BRAND / "preview.svg", preview_svg)
    write(BRAND / "preview.png", resvg_py.svg_to_bytes(svg_string=preview_svg, width=1200))

    # Directly rendered small sizes; no resized JPEG or embedded raster in the SVG masters.
    ico_parts = [(size, png(icon, size)) for size in [16, 24, 32, 48, 64, 128, 256]]
    offset = 6 + 16 * len(ico_parts)
    entries = []
    for size, data in ico_parts:
        entries.append(struct.pack("<BBBBHHII", size % 256, size % 256, 0, 0, 1, 32, len(data), offset))
        offset += len(data)
    write(BRAND / "web/favicon.ico", struct.pack("<HHH", 0, 1, len(entries)) + b"".join(entries) + b"".join(data for _, data in ico_parts))
    write(BRAND / "web/favicon.svg", icon)
    for filename, size in [("favicon-16.png", 16), ("favicon-32.png", 32), ("apple-touch-icon.png", 180),
                           ("icon-192.png", 192), ("icon-512.png", 512)]:
        write(BRAND / "web" / filename, png(icon, size))
    # A distinct smaller mark stays within the circular maskable-icon safe region.
    mask_scale = 600 / symbol["height"]
    maskable = svg(1024, 1024, f'<rect width="1024" height="1024" fill="{brand["iconBackground"]}"/>'
        f'<g transform="translate({(1024-symbol["width"]*mask_scale)/2:g} 212) scale({mask_scale:g})">{path_markup(symbol["paths"])}</g>', "Generosity maskable icon")
    write(BRAND / "web/icon-maskable-512.png", png(maskable, 512))
    json_file(BRAND / "web/manifest.webmanifest", {"name": "Generosity", "short_name": "Generosity", "start_url": "./",
        "display": "standalone", "background_color": brand["iconBackground"], "theme_color": orange,
        "icons": [{"src": f"icon-{size}.png", "sizes": f"{size}x{size}", "type": "image/png", "purpose": "any"} for size in [192, 512]] +
        [{"src": "icon-maskable-512.png", "sizes": "512x512", "type": "image/png", "purpose": "maskable"}]})

    res = ROOT / "androidApp/src/main/res"
    write(res / "drawable/generosity_mark.xml", android_vector(symbol, intrinsic=(36, 48)))
    write(res / "drawable/generosity_wordmark.xml", android_vector(wordmark, intrinsic=(180, 180 * wordmark["height"] / wordmark["width"])))
    for folder, color in [("values", TEXT_DARK), ("values-night", TEXT_LIGHT)]:
        write(res / folder / "brand_colors.xml", f'<resources><color name="generosity_wordmark_text">{color}</color><color name="generosity_icon_background">{brand["iconBackground"]}</color></resources>\n')
    scale = 50 / symbol["height"]
    offset = ((108 - symbol["width"] * scale) / 2, 29)
    for name, mono in [("generosity_launcher_foreground", False), ("generosity_launcher_monochrome", True)]:
        write(res / "drawable" / (name + ".xml"), android_vector(symbol, (108, 108), offset, scale, mono))
    for folder in ["mipmap-anydpi-v26", "mipmap-anydpi-v33"]:
        mono = '\n<monochrome android:drawable="@drawable/generosity_launcher_monochrome"/>' if folder.endswith("v33") else ""
        write(res / folder / "ic_launcher.xml", '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
              '<background android:drawable="@color/generosity_icon_background"/>\n'
              '<foreground android:drawable="@drawable/generosity_launcher_foreground"/>' + mono + '\n</adaptive-icon>\n')
    for density, size in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]:
        write(res / f"mipmap-{density}/ic_launcher.png", png(icon, size))
    json_file(ROOT / "iosApp/GenerosityApp/Assets.xcassets/Contents.json", {"info": {"author": "xcode", "version": 1}})
    image_set("GenerosityWordmark", [(False, outputs["wordmark"]), (True, outputs["wordmark-dark"])])
    image_set("GenerositySymbol", [(False, outputs["symbol"])])
    appicon = ROOT / "iosApp/GenerosityApp/Assets.xcassets/AppIcon.appiconset"
    # iPhone/iPad slots keep the project's iOS 16 deployment target supported.
    slots = [("iphone", size, scale) for size, scales in [(20, [2, 3]), (29, [2, 3]), (40, [2, 3]), (60, [2, 3])] for scale in scales]
    slots += [("ipad", size, scale) for size, scales in [(20, [1, 2]), (29, [1, 2]), (40, [1, 2]), (76, [1, 2]), (83.5, [2])] for scale in scales]
    slots += [("ios-marketing", 1024, 1)]
    for size in sorted({int(size * scale) for _, size, scale in slots}):
        raster = Image.open(BytesIO(png(icon, size))).convert("RGB")
        target = appicon / f"icon-{size}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        raster.save(target)
    json_file(appicon / "Contents.json", {"info": {"author": "xcode", "version": 1}, "images": [
        {"idiom": idiom, "size": f"{size:g}x{size:g}", "scale": f"{scale}x", "filename": f"icon-{int(size*scale)}.png"}
        for idiom, size, scale in slots]})

    # Verify actual vector output against the supplied silhouettes and format contracts.
    quality = None
    if args.trace_source:
        quality = {}
        for name, geometry, mask in [("symbol", symbol, symbol_mask), ("wordmark", wordmark, wordmark_mask)]:
            rendered = Image.open(BytesIO(png(vector(geometry, "Generosity"), geometry["width"]))).getchannel("A").point(lambda p: 255 if p >= 128 else 0)
            intersection = sum(p > 0 for p in ImageChops.darker(mask, rendered).get_flattened_data())
            union = sum(p > 0 for p in ImageChops.lighter(mask, rendered).get_flattened_data())
            quality[name] = round(intersection / union, 5)
            assert quality[name] > 0.985, f"Trace deviates from reference: {name} {quality[name]}"
    for path in (BRAND / "svg").glob("*.svg"):
        root = ET.fromstring(path.read_text())
        assert not root.findall(f".//{{{SVG_NS}}}image") and not root.findall(f".//{{{SVG_NS}}}text")
    assert Image.open(appicon / "icon-1024.png").mode == "RGB"
    assert Image.open(BRAND / "web/favicon.ico").ico.sizes() == {(s, s) for s, _ in ico_parts}
    assert ((symbol["width"] * scale / 2) ** 2 + 25 ** 2) ** .5 < 33, "Android foreground exceeds safe circle"
    json_file(BRAND / "verification.json", {"source": "master/symbol.svg and master/wordmark.svg", "output_profile": "sRGB",
        "orange_srgb": orange, "silhouette_intersection_over_union": quality, "ico_sizes": [s for s, _ in ico_parts],
        "svg_contains_embedded_raster_or_font_text": False, "ios_marketing_icon_has_alpha": False})
    # The same masters feed the public site, native headers, favicons and app icons.
    for source, target in [("png/stacked.png", "logo.png"), ("web/favicon-32.png", "favicon.png"),
                           ("web/apple-touch-icon.png", "apple-touch-icon.png")]:
        write(ROOT / "portal/assets" / target, (BRAND / source).read_bytes())
    with ZipFile(BRAND / "generosity-brand-kit.zip", "w", ZIP_DEFLATED) as archive:
        for folder in ["svg", "png", "web"]:
            for path in sorted((BRAND / folder).rglob("*")):
                if path.is_file():
                    archive.writestr(ZipInfo(str(path.relative_to(BRAND))), path.read_bytes(), compress_type=ZIP_DEFLATED)
        for filename in ["README.md", "verification.json", "preview.svg", "preview.png"]:
            archive.writestr(ZipInfo(filename), (BRAND / filename).read_bytes(), compress_type=ZIP_DEFLATED)
        for pattern in ["drawable/generosity_*.xml", "values*/brand_colors.xml", "mipmap-*/ic_launcher.*"]:
            for path in sorted(res.glob(pattern)):
                archive.writestr(ZipInfo("android/res/" + str(path.relative_to(res))), path.read_bytes(), compress_type=ZIP_DEFLATED)
        assets = ROOT / "iosApp/GenerosityApp/Assets.xcassets"
        for path in sorted(assets.rglob("*")):
            if path.is_file():
                archive.writestr(ZipInfo("ios/Assets.xcassets/" + str(path.relative_to(assets))), path.read_bytes(), compress_type=ZIP_DEFLATED)
    print(json.dumps({"output": str(BRAND), "orange": orange, "silhouette_iou": quality}, indent=2))


if __name__ == "__main__":
    main()
