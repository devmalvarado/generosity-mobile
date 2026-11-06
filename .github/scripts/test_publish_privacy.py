import unittest
from pathlib import Path
from unittest.mock import patch

import publish_privacy as release


class FTP:
    def __init__(self):
        self.files = {release.ROOT + '/' + name: b'previous ' + name.encode() for name in release.NOTICES}
        self.files['/private/.env'] = b'untouched'
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


if __name__ == '__main__':
    unittest.main()
