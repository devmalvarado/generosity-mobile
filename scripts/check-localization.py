#!/usr/bin/env python3
"""Validate every enabled mobile language using only the Python standard library."""
from collections import Counter
import json
from pathlib import Path
import plistlib
import re
import sys
import unicodedata
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
STRING = re.compile(r'("(?:[^"\\]|\\.)*")\s*=\s*("(?:[^"\\]|\\.)*")\s*;')
FORMAT = re.compile(r'%(?:(\d+)\$)?([dsf@])')


def require(condition, message):
    if not condition:
        raise ValueError(message)


def text(path):
    value = path.read_text(encoding='utf-8')
    require(value == unicodedata.normalize('NFC', value), f'{path}: use NFC Unicode')
    require('\ufffd' not in value and not re.search('[\u00c2\u00c3][\x80-\xbf]', value),
            f'{path}: damaged Unicode characters')
    return value


def unique(pairs, source):
    result = {}
    for key, value in pairs:
        require(key not in result, f'{source}: duplicate key {key}')
        require(bool(value), f'{source}: empty value for {key}')
        result[key] = value
    return result


def apple_strings(path):
    source = text(path)
    source = re.sub(r'/\*.*?\*/|^\s*//[^\n]*', '', source, flags=re.S | re.M)
    require(not STRING.sub('', source).strip(), f'{path}: invalid .strings syntax')
    return unique(((json.loads(m[1]), json.loads(m[2])) for m in STRING.finditer(source)), path)


def android_strings(path):
    return unique(((node.attrib['name'], ''.join(node.itertext()))
                   for node in ET.fromstring(text(path)).findall('string')), path)


def signature(value):
    value = value.replace('%%', '')
    return Counter((int(match[1] or index), match[2])
                   for index, match in enumerate(FORMAT.finditer(value), 1))


def coverage(values, baseline, source):
    require(values.keys() == baseline.keys(),
            f'{source}: missing {sorted(baseline.keys() - values.keys())}; extra {sorted(values.keys() - baseline.keys())}')
    for key, value in values.items():
        require(signature(value) == signature(baseline[key]), f'{source}: placeholder mismatch for {key}')


def android_plurals(path):
    return unique(((node.attrib['name'], unique(((item.attrib['quantity'], item.text)
                   for item in node.findall('item')), path))
                   for node in ET.fromstring(text(path)).findall('plurals')), path)


def apple_plurals(path):
    data = plistlib.loads(text(path).encode('utf-8'))
    result = {}
    for key, entry in data.items():
        token = re.fullmatch(r'%#@(\w+)@', entry['NSStringLocalizedFormatKey'])
        require(token is not None, f'{path}: expected one plural argument for {key}')
        rule = entry[token[1]]
        require(rule['NSStringFormatSpecTypeKey'] == 'NSStringPluralRuleType', f'{path}: invalid plural rule')
        require(rule['NSStringFormatValueTypeKey'] == 'd', f'{path}: expected integer count')
        result[key] = {category: value for category, value in rule.items() if not category.startswith('NSString')}
    return result


def validate():
    model = (ROOT / 'shared/src/commonMain/kotlin/org/generosity/domain/Models.kt').read_text()
    registry = model.split('enum class SupportedLocale', 1)[1].split('companion object', 1)[0]
    languages = re.findall(r'\b[A-Z][A-Z0-9_]*\("([a-zA-Z0-9-]+)",', registry)
    require(languages and len(languages) == len(set(languages)), 'Invalid language registry')
    android = ROOT / 'androidApp/src/main/res'
    ios = ROOT / 'iosApp/GenerosityApp'
    baseline = android_strings(android / 'values/strings.xml')
    plural_baseline = android_plurals(android / 'values/plurals.xml')
    permission_baseline = apple_strings(ios / 'en.lproj/InfoPlist.strings')
    require(apple_strings(ios / 'en.lproj/Localizable.strings') == baseline, 'English platforms disagree')
    project = (ROOT / 'iosApp/project.yml').read_text()
    regions = re.search(r'knownRegions:\s*\[([^]]+)\]', project)[1].replace(' ', '').split(',')
    for language in languages:
        require(language in regions, f'{language}: missing Xcode knownRegion')
        folder = 'values' if language == 'en' else 'values-' + ('b+' + language.replace('-', '+') if '-' in language else language)
        android_values = android_strings(android / folder / 'strings.xml')
        apple_values = apple_strings(ios / f'{language}.lproj/Localizable.strings')
        coverage(android_values, baseline, language + ' Android')
        coverage(apple_values, baseline, language + ' iOS')
        require(android_values == apple_values, f'{language}: Android/iOS copy differs')
        coverage(apple_strings(ios / f'{language}.lproj/InfoPlist.strings'), permission_baseline, language + ' permissions')
        ap = android_plurals(android / folder / 'plurals.xml')
        ip = apple_plurals(ios / f'{language}.lproj/Localizable.stringsdict')
        require(ap.keys() == ip.keys() == plural_baseline.keys(), f'{language}: plural keys differ')
        for key, forms in ap.items():
            require('other' in forms and forms.keys() == ip[key].keys(), f'{language}: incomplete plural forms')
            for category, value in forms.items():
                # Some languages spell out zero/one without repeating the numeric argument.
                require(signature(value) in (Counter(), signature(plural_baseline[key]['other'])), f'{language}/{key}: plural placeholder mismatch')
                require(value.replace('%1$d', '%d') == ip[key][category], f'{language}/{key}: plural copy differs')
    print(f'Localization OK: {", ".join(languages)}; {len(baseline)} strings, plurals, permissions, UTF-8/NFC and placeholders.')


def self_test():
    require(signature('%1$d item') == signature('Falta %d unidad'), 'Number placeholder comparison')
    require(signature('%1$d') != signature('%1$s'), 'Wrong placeholder type must fail')
    for action in [lambda: unique([('a', 'A'), ('a', 'B')], 'test'),
                   lambda: coverage({'a': 'A'}, {'a': 'A', 'b': 'B'}, 'test'),
                   lambda: coverage({'a': '%s'}, {'a': '%d'}, 'test')]:
        try:
            action()
        except ValueError:
            continue
        raise AssertionError('Invalid catalog was accepted')
    print('Localization validator self-check passed.')


if __name__ == '__main__':
    try:
        if '--self-test' in sys.argv:
            self_test()
        validate()
    except (ValueError, KeyError, OSError, ET.ParseError, plistlib.InvalidFileException) as error:
        sys.exit(str(error))
