#!/usr/bin/env python3
"""Loopback-only static preview with an optional, read-only public catalog relay."""
import argparse
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import re
from urllib.error import HTTPError
from urllib.parse import parse_qs, unquote, urlsplit
from urllib.request import Request, build_opener, HTTPRedirectHandler

ROOT = Path(__file__).resolve().parents[1] / 'portal'


def public_path(path):
    url = urlsplit(path)
    if url.scheme or url.netloc or url.fragment:
        return False
    query = parse_qs(url.query, keep_blank_values=True)
    if url.path == '/api/v1/sync/catalog':
        return query == {'category_version': ['2'], 'progress_version': ['1']}
    return bool(re.fullmatch(r'/api/v1/(centers|needs)/[a-zA-Z0-9][a-zA-Z0-9-]{0,99}', url.path) and not url.query)


class NoRedirect(HTTPRedirectHandler):
    def redirect_request(self, *args, **kwargs):
        return None


class PortalHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        expected = {'127.0.0.1:' + str(self.server.server_port), 'localhost:' + str(self.server.server_port)}
        if self.headers.get('Host') not in expected:
            return self.send_error(403)
        if self.path.startswith('/__public/'):
            path = self.path[len('/__public'):]
            if not self.server.live_catalog or not public_path(path):
                return self.send_error(404)
            origin = self.headers.get('Origin')
            if origin and origin not in {'http://' + host for host in expected}:
                return self.send_error(403)
            try:
                # No browser credentials, request headers, cookies or redirects cross this boundary.
                request = Request('https://api.generosity.global' + path, headers={'Accept': 'application/json'})
                with build_opener(NoRedirect).open(request, timeout=12) as response:
                    body = response.read(8 * 1024 * 1024 + 1)
                    if len(body) > 8 * 1024 * 1024 or 'application/json' not in response.headers.get('Content-Type', ''):
                        return self.send_error(502)
                    return self.reply(body, 'application/json')
            except HTTPError as error:
                return self.send_error(404 if error.code == 404 else 502)
            except (OSError, TimeoutError):
                return self.send_error(502)
        path = unquote(urlsplit(self.path).path).lstrip('/')
        files = (ROOT / 'public-files.txt').read_text().splitlines()
        if path.rstrip('/') + '/index.html' in files and not path.endswith('/'):
            self.send_response(301)
            self.send_header('Location', '/' + path + '/')
            self.end_headers()
            return
        name = path + 'index.html' if not path or path.endswith('/') else path
        if name not in files:
            return self.send_error(404)
        if name.endswith('.html') and self.server.live_catalog:
            html = (ROOT / name).read_text()
            if 'name="generosity-api-origin"' in html:
                html = html.replace('<head>', '<head>\n  <meta name="generosity-public-api-origin" content="/__public">', 1)
            return self.reply(html.encode(), 'text/html; charset=utf-8')
        super().do_GET()

    def reply(self, body, content_type):
        self.send_response(200)
        self.send_header('Content-Type', content_type)
        self.send_header('Content-Length', str(len(body)))
        self.send_header('Cache-Control', 'no-store')
        self.send_header('X-Content-Type-Options', 'nosniff')
        self.end_headers()
        self.wfile.write(body)


def self_test():
    assert public_path('/api/v1/sync/catalog?category_version=2&progress_version=1')
    assert public_path('/api/v1/centers/centro-norte')
    assert public_path('/api/v1/needs/1234-abcd')
    for path in ['/auth/session', '/api/v1/me/contributions', '/api/v1/staff/centers',
                 '//other.test/api/v1/centers/a', '/api/v1/centers/../users',
                 '/api/v1/centers/a?include=private', '/api/v1/sync/catalog?category_version=2&progress_version=1&token=secret',
                 '/api/v1/centers/%2e%2e', '/api/v1/centers/a#fragment']:
        assert not public_path(path), path
    assert not hasattr(PortalHandler, 'do_POST')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--port', type=int, default=47321)
    parser.add_argument('--live-catalog', action='store_true')
    parser.add_argument('--self-test', action='store_true')
    args = parser.parse_args()
    if args.self_test:
        self_test()
        print('Public catalog relay boundary checks passed.')
    else:
        server = ThreadingHTTPServer(('127.0.0.1', args.port), partial(PortalHandler, directory=str(ROOT)))
        server.live_catalog = args.live_catalog
        print(f'Portal: http://127.0.0.1:{args.port}/ayudar/', flush=True)
        server.serve_forever()
