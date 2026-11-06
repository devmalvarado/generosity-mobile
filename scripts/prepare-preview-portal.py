#!/usr/bin/env python3
"""Package the existing portal for the isolated preview API; never deploy it."""
import argparse
from pathlib import Path
import re
import shutil
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parents[1]


def configure(html, origin):
    url = urlsplit(origin)
    if (not re.fullmatch(r'https://[A-Za-z0-9.-]+(:[0-9]+)?', origin)
            or url.scheme != 'https' or not url.hostname or url.username or url.password
            or url.path or url.query or url.fragment
            or url.hostname.rstrip('.') in ('api.generosity.global', 'generosity.global', 'www.generosity.global')):
        raise ValueError('Preview requires a separate HTTPS API origin')
    for key, value in [('generosity-api-origin', origin), ('generosity-channel', 'preview')]:
        html, count = re.subn(r'(<meta name="' + key + r'" content=")[^"]*(")', lambda m: m[1] + value + m[2], html)
        if count != 1:
            raise ValueError('Missing or duplicate portal environment metadata: ' + key)
    return html.replace('<title>Generosity', '<title>Generosity Pruebas', 1)


def self_test():
    html = '<meta name="generosity-api-origin" content="https://api.generosity.global"><meta name="generosity-channel" content="production">'
    assert 'content="preview"' in configure(html, 'https://api-preview.generosity.global')
    for origin in ['https://api.generosity.global', 'https://API.GENEROSITY.GLOBAL.', 'http://localhost', 'https://user@preview.test', 'https://preview.test/path', 'https://preview.test?a=1', 'https://preview.test/"']:
        try:
            configure(html, origin)
        except ValueError:
            continue
        raise AssertionError('Unsafe origin accepted: ' + origin)
    try:
        configure('', 'https://preview.test')
    except ValueError:
        return
    raise AssertionError('Missing metadata accepted')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--api-origin', default='https://api-preview.generosity.global')
    parser.add_argument('--self-test', action='store_true')
    args = parser.parse_args()
    if args.self_test:
        self_test()
        print('Preview portal configuration checks passed.')
    else:
        source = ROOT / 'portal'
        # Validate the API even if the inventory later contains only showcase pages.
        configure((source / 'index.html').read_text(), args.api_origin)
        output = source / 'build/preview'
        if output.exists():
            shutil.rmtree(output)  # Generated package must not retain files from an older inventory.
        for name in (source / 'public-files.txt').read_text().splitlines():
            path = Path(name)
            if path.is_absolute() or '..' in path.parts:
                raise ValueError('Public file must stay inside the portal: ' + name)
            destination = output / path
            destination.parent.mkdir(parents=True, exist_ok=True)
            if path.suffix == '.html' and 'name="generosity-api-origin"' in (source / path).read_text():
                destination.write_text(configure((source / path).read_text(), args.api_origin))
            else:
                shutil.copyfile(source / path, destination)
        print(output)
