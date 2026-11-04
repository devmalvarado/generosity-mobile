#!/usr/bin/env python3
"""Fail an iOS preview build that would use production identity or Firebase."""
import os
import sys
import tempfile
from pathlib import Path
import plistlib
from urllib.parse import urlsplit


def validate(env):
    channel = env['GENEROSITY_CHANNEL']
    preview = channel == 'preview'
    app_id = 'org.generosity.app' + ('.preview' if preview else '')
    url = urlsplit(env['GENEROSITY_API_URL'])
    if (channel not in ('production', 'preview')
            or env['PRODUCT_BUNDLE_IDENTIFIER'] != app_id
            or env['GENEROSITY_AUTH_SCHEME'] != app_id + '.ios'
            or env['GENEROSITY_AUTH_CLIENT'] != ('ios-preview' if preview else 'ios')
            or url.scheme != 'https' or not url.hostname or url.username or url.password
            or url.path or url.query or url.fragment
            or (preview and url.hostname.rstrip('.') in ('api.generosity.global', 'generosity.global', 'www.generosity.global'))):
        raise ValueError('Invalid mobile environment identity or API origin')
    firebase = Path(env['SRCROOT']) / env['GENEROSITY_FIREBASE_PLIST_FILE']
    if firebase.exists():
        config = plistlib.loads(firebase.read_bytes())
        if config['BUNDLE_ID'] != app_id or (preview and config['PROJECT_ID'] == 'generosity-v01'):
            raise ValueError('Firebase must belong to the selected app and environment')


def self_test():
    with tempfile.TemporaryDirectory() as folder:
        env = dict(GENEROSITY_CHANNEL='preview', PRODUCT_BUNDLE_IDENTIFIER='org.generosity.app.preview',
                   GENEROSITY_AUTH_CLIENT='ios-preview', GENEROSITY_AUTH_SCHEME='org.generosity.app.preview.ios',
                   GENEROSITY_API_URL='https://api-preview.generosity.global', SRCROOT=folder,
                   GENEROSITY_FIREBASE_PLIST_FILE='Firebase.plist')
        validate(env)
        for key, value in [('GENEROSITY_API_URL', 'https://api.generosity.global'),
                           ('GENEROSITY_API_URL', 'https://API.GENEROSITY.GLOBAL.'),
                           ('PRODUCT_BUNDLE_IDENTIFIER', 'org.generosity.app'),
                           ('GENEROSITY_AUTH_SCHEME', 'org.generosity.app.ios'),
                           ('GENEROSITY_AUTH_CLIENT', 'ios')]:
            try:
                validate(dict(env, **{key: value}))
            except ValueError:
                continue
            raise AssertionError('Unsafe preview build accepted: ' + key)
        Path(folder, 'Firebase.plist').write_bytes(plistlib.dumps(dict(BUNDLE_ID='org.generosity.app.preview', PROJECT_ID='generosity-v01')))
        try:
            validate(env)
        except ValueError:
            return
        raise AssertionError('Production Firebase project accepted')


if __name__ == '__main__':
    if '--self-test' in sys.argv:
        self_test()
    else:
        validate(os.environ)
    print('Mobile environment isolation checked.')
