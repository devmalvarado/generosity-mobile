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
    record = r'[a-zA-Z0-9][a-zA-Z0-9-]{0,99}'
    if re.fullmatch(rf'/api/v1/(centers|needs)/{record}', url.path):
        return not url.query
    if re.fullmatch(rf'/api/v1/needs/{record}/progress', url.path):
        return not url.query
    if re.fullmatch(r'/api/v1/(campaigns|occurrences|meals)', url.path):
        page = query.get('page')
        return list(query) == ['page'] and len(page) == 1 and bool(re.fullmatch(r'[1-9][0-9]{0,3}', page[0]))
    if re.fullmatch(rf'/api/v1/(campaigns|occurrences|meals)/{record}', url.path):
        return not url.query
    return False


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
    assert public_path('/api/v1/needs/1234-abcd/progress')
    assert public_path('/api/v1/campaigns?page=1')
    assert public_path('/api/v1/occurrences?page=2')
    assert public_path('/api/v1/meals?page=25')
    assert public_path('/api/v1/campaigns/44444444-4444-4444-8444-444444444444')
    assert public_path('/api/v1/occurrences/55555555-5555-4555-8555-555555555555')
    assert public_path('/api/v1/meals/66666666-6666-4666-8666-666666666666')
    for path in ['/auth/session', '/api/v1/me/contributions', '/api/v1/me/notifications',
                 '/api/v1/me/notification-preferences', '/api/v1/staff/centers',
                 '/api/v1/opportunities/campaign/44444444-4444-4444-8444-444444444444',
                 '/api/v1/outcome-updates/77777777-7777-4777-8777-777777777777',
                 '//other.test/api/v1/centers/a', '/api/v1/centers/../users',
                 '/api/v1/centers/a?include=private', '/api/v1/sync/catalog?category_version=2&progress_version=1&token=secret',
                 '/api/v1/centers/%2e%2e', '/api/v1/centers/a#fragment',
                 '/api/v1/campaigns', '/api/v1/campaigns?page=0', '/api/v1/campaigns?page=01',
                 '/api/v1/campaigns?page=1&token=secret', '/api/v1/campaigns?page=1&page=2',
                 '/api/v1/needs/1234-abcd/progress?fulfilled=true',
                 '/api/v1/meals/66666666-6666-4666-8666-666666666666/commit']:
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
