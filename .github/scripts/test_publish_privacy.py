import unittest
from pathlib import Path
from unittest.mock import patch, MagicMock

import publish_privacy as release


class FTP:
    def __init__(self):
        self.files = {release.ROOT + '/' + name: b'previous ' + name.encode() for name in release.NOTICES}
        self.files['/private/.env'] = b'untouched'
        self.files['/private/config/cors.php'] = b'previous cors'
        self.fail = None

    def retrbinary(self, command, callback):
        callback(self.files[command.removeprefix('RETR ')])

    def storbinary(self, command, stream):
        path = command.removeprefix('STOR ')
        self.files[path] = stream.read()
        if path == self.fail:
            self.files[path] = b'interrupted'

    def rename(self, source, destination):
        self.files[destination] = self.files.pop(source)

    def mkd(self, path):
        pass

    def delete(self, path):
        self.files.pop(path, None)

    def sendcmd(self, command):
        pass


class PrivacyReleaseTest(unittest.TestCase):
    @patch.object(Path, 'read_bytes', return_value=b'OpenStreetMap notice')
    def test_only_notices_change_and_originals_are_backed_up(self, _):
        ftp = FTP()
        previous = dict(ftp.files)
        release.publish(ftp, '123-1')
        for name in release.NOTICES:
            self.assertEqual(ftp.files[release.ROOT + '/' + name], b'OpenStreetMap notice')
            self.assertEqual(ftp.files['/private/.deployments/portal-privacy-123-1/' + name], previous[release.ROOT + '/' + name])
        self.assertEqual(ftp.files['/private/.env'], b'untouched')
        release.publish(ftp, '123-2')  # No-op retry.

    @patch.object(Path, 'read_bytes', return_value=b'OpenStreetMap notice')
    def test_interrupted_upload_restores_the_first_notice(self, _):
        ftp = FTP()
        previous = dict(ftp.files)
        ftp.fail = release.ROOT + '/privacy.en.md.new'
        with self.assertRaises(RuntimeError):
            release.publish(ftp, '123-1')
        for path, body in previous.items():
            self.assertEqual(ftp.files[path], body)

    @patch.object(Path, 'read_bytes', return_value=b'updated cors')
    def test_uncertain_cache_refresh_retains_valid_source_backup_and_removes_helper(self, _):
        ftp = FTP()
        preflight = MagicMock()
        preflight.__enter__.return_value.headers = {}
        with patch.object(release, 'urlopen', side_effect=[preflight, RuntimeError('connection failed')]):
            with self.assertRaises(RuntimeError):
                release.refresh_cors(ftp, '123-1')
        self.assertEqual(ftp.files['/private/config/cors.php'], b'updated cors')
        self.assertEqual(ftp.files['/private/.deployments/portal-cors-123-1/cors.php'], b'previous cors')
        self.assertEqual(ftp.files['/private/.env'], b'untouched')
        self.assertFalse(any(path.startswith('/public_html/') for path in ftp.files))

    def test_supported_preflight_does_not_touch_server_files(self):
        ftp = FTP()
        previous = dict(ftp.files)
        response = MagicMock()
        response.__enter__.return_value.headers = {'Access-Control-Allow-Headers': 'accept, x-generosity-channel'}
        with patch.object(release, 'urlopen', return_value=response):
            release.refresh_cors(ftp, '123-1')
        self.assertEqual(ftp.files, previous)


if __name__ == '__main__':
    unittest.main()
