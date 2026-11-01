#!/usr/bin/env python3
"""Check that a single design edit reaches all consumers without unsafe values."""
import copy
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
import shutil
import subprocess
import sys

spec = importlib.util.spec_from_file_location("design", Path(__file__).with_name("generate-design-system.py"))
design = importlib.util.module_from_spec(spec)
spec.loader.exec_module(design)


class DesignTest(unittest.TestCase):
    @unittest.skipUnless(importlib.util.find_spec('PIL') and importlib.util.find_spec('resvg_py'), 'Use the brand-authoring virtualenv for raster export checks')
    def test_brand_edit_updates_native_and_web_exports_deterministically(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            for name in ['scripts/generate-design-system.py', 'scripts/generate-brand-assets.py',
                         'design/tokens.json', 'assets/brand/README.md',
                         'assets/brand/master/symbol.svg', 'assets/brand/master/wordmark.svg']:
                (root / name).parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(design.ROOT / name, root / name)
            source = root / 'design/tokens.json'
            data = json.loads(source.read_text())
            data['brand']['color'] = '#184C70'
            source.write_text(json.dumps(data))
            command = [sys.executable, str(root / 'scripts/generate-design-system.py')]
            subprocess.run(command, check=True, capture_output=True)
            for name in ['assets/brand/svg/symbol.svg',
                         'androidApp/src/main/res/drawable/generosity_mark.xml',
                         'iosApp/GenerosityApp/Assets.xcassets/GenerositySymbol.imageset/GenerositySymbol.svg']:
                self.assertIn('#184C70', (root / name).read_text(), name)
            self.assertNotEqual((root / 'portal/assets/logo.png').read_bytes(), (design.ROOT / 'portal/assets/logo.png').read_bytes())
            original = (root / 'design/generated.json').read_bytes()
            # Force a second full export; PNGs, native resources and ZIP must be identical.
            (root / 'design/generated.json').unlink()
            subprocess.run(command, check=True, capture_output=True)
            self.assertEqual(original, (root / 'design/generated.json').read_bytes())
            subprocess.run(command + ['--check'], check=True, capture_output=True)

    def test_one_action_color_changes_all_four_surfaces(self):
        data = design.load_tokens()
        original = design.render(data)
        data["colors"]["accent"][0] = "#184C70"
        updated = design.render(data)
        self.assertEqual(len(updated), 4)
        for path, content in updated.items():
            self.assertNotEqual(content, original[path], path)
            self.assertIn("184C70", content, path)

    def test_type_and_geometry_changes_reach_native_and_web(self):
        data = design.load_tokens()
        data["typography"]["android"]["body"] = "serif"
        data["typography"]["ios"]["body"] = "Georgia"
        data["typography"]["web"]["body"] = "Georgia, serif"
        data["radii"]["card"] = 18
        outputs = design.render(data)
        android, ios, web, auth = outputs.values()
        self.assertIn('const val body = "serif"', android)
        self.assertIn('static let body = "Georgia"', ios)
        self.assertIn('val card = 18.dp', android)
        self.assertIn('static let card: CGFloat = 18', ios)
        self.assertIn('--gen-font-body: Georgia, serif', web)
        self.assertIn('--gen-radius-card: 18px', web)
        self.assertNotIn('@import', auth)

    def test_invalid_colors_fonts_and_small_targets_fail_before_generation(self):
        base = design.load_tokens()
        for path, value in [(('colors', 'accent'), ['red', '#000000']),
                            (('colors', 'accent'), ['#FFFFFF', '#FFB197']),
                            (('sizes', 'iosTouch'), 20),
                            (('typography', 'web', 'body'), 'serif;}body{display:none'),
                            (('typography', 'android', 'body'), 'bad"name'),
                            (('typography', 'web', 'stylesheet'), 'https://example.com/fonts.css')]:
            with self.subTest(path=path), tempfile.TemporaryDirectory() as folder:
                data = copy.deepcopy(base)
                cursor = data
                for part in path[:-1]:
                    cursor = cursor[part]
                cursor[path[-1]] = value
                source = Path(folder) / 'tokens.json'
                source.write_text(json.dumps(data))
                with self.assertRaises(ValueError):
                    design.load_tokens(source)

    def test_check_detects_stale_theme_and_modified_logo(self):
        manifest = json.loads(design.MANIFEST.read_text())
        paths = set(manifest['files']) | set(design.render(design.load_tokens())) | {
            'scripts/generate-design-system.py', 'scripts/generate-brand-assets.py',
            'design/tokens.json', 'design/generated.json', 'assets/brand/README.md',
            'assets/brand/master/symbol.svg', 'assets/brand/master/wordmark.svg'
        }
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            for name in paths:
                (root / name).parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(design.ROOT / name, root / name)
            command = [sys.executable, str(root / 'scripts/generate-design-system.py'), '--check']
            self.assertEqual(subprocess.run(command, capture_output=True).returncode, 0)
            css = root / 'portal/assets/design-tokens.css'
            original = css.read_bytes()
            css.write_text('stale')
            self.assertNotEqual(subprocess.run(command, capture_output=True).returncode, 0)
            css.write_bytes(original)
            (root / 'portal/assets/logo.png').write_bytes(b'stale logo')
            self.assertNotEqual(subprocess.run(command, capture_output=True).returncode, 0)


if __name__ == '__main__':
    unittest.main()
