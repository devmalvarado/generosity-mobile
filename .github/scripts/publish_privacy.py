"""Publish only the two map-provider notices; preserve all API code and runtime data."""
import io
import os
from pathlib import Path

from publish_api import connect, require_env

NOTICES = ('privacy.es.md', 'privacy.en.md')
ROOT = '/private/resources/legal'


def read(ftp, path):
    body = io.BytesIO()
    ftp.retrbinary('RETR ' + path, body.write)
    return body.getvalue()


def replace(ftp, path, body):
    staged = path + '.new'
    ftp.storbinary('STOR ' + staged, io.BytesIO(body))
    if read(ftp, staged) != body:
        raise RuntimeError('Privacy upload verification failed before replacement')
    ftp.rename(staged, path)


def publish(ftp, release):
    if not release or any(c not in '0123456789-' for c in release):
        raise ValueError('Expected a numeric workflow run and attempt')
    desired = {name: (Path('backend/resources/legal') / name).read_bytes() for name in NOTICES}
    if not all(b'OpenStreetMap' in body for body in desired.values()):
        raise ValueError('Both privacy notices must disclose the map provider')
    previous = {name: read(ftp, ROOT + '/' + name) for name in NOTICES}
    changed = [name for name in NOTICES if previous[name] != desired[name]]
    if not changed:
        print('Privacy notices already current')
        return
    backup = '/private/.deployments/portal-privacy-' + release
    ftp.mkd(backup)
    ftp.sendcmd('SITE CHMOD 700 ' + backup)
    for name in changed:
        target = backup + '/' + name
        ftp.storbinary('STOR ' + target, io.BytesIO(previous[name]))
        if read(ftp, target) != previous[name]:
            raise RuntimeError('Privacy backup verification failed; originals unchanged')
    completed = []
    try:
        for name in changed:
            replace(ftp, ROOT + '/' + name, desired[name])
            completed.append(name)
    except Exception:
        for name in completed:
            replace(ftp, ROOT + '/' + name, previous[name])
        raise
    print('Published privacy notices; backup: ' + backup)


if __name__ == '__main__':
    with connect(require_env('API_FTP_SERVER'), require_env('API_FTP_USERNAME'), require_env('API_FTP_PASSWORD')) as ftp:
        publish(ftp, os.environ['GITHUB_RUN_ID'] + '-' + os.environ['GITHUB_RUN_ATTEMPT'])
