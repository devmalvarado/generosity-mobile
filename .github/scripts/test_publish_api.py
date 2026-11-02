import copy
import json
import os
import tempfile
import unittest
import zipfile
from pathlib import Path
from unittest.mock import patch

import publish_api


class InitialCatalogReleaseTest(unittest.TestCase):
    def test_import_requires_explicit_opt_in_and_stays_outside_public_root(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(publish_api, 'private_files', return_value=[]):
            archive = Path(directory) / 'release.zip'
            self.assertEqual(publish_api.build_zip(archive), [])
            self.assertEqual(publish_api.build_zip(archive, True), ['private/data/initial-catalog.json'])
            with zipfile.ZipFile(archive) as z:
                self.assertEqual(len(json.loads(z.read('private/data/initial-catalog.json'))['centers']), 37)

    def test_unreviewed_batch_shapes_are_rejected_before_upload(self):
        original = json.loads(Path('data/initial-catalog/catalog.json').read_text())
        invalid = []
        for field, value in [('verificationStatus', 'officially_verified'), ('needs', [{'id': 'invented'}])]:
            document = copy.deepcopy(original)
            document['centers'][0][field] = value
            invalid.append(document)
        duplicate = copy.deepcopy(original)
        duplicate['centers'][1]['id'] = duplicate['centers'][0]['id']
        invalid.append(duplicate)
        invalid.append({'schemaVersion': 1, 'centers': []})
        previous = Path.cwd()
        try:
            with tempfile.TemporaryDirectory() as directory, patch.object(publish_api, 'private_files', return_value=[]):
                os.chdir(directory)
                Path('data/initial-catalog').mkdir(parents=True)
                for document in invalid:
                    Path('data/initial-catalog/catalog.json').write_text(json.dumps(document))
                    with self.assertRaises(SystemExit):
                        publish_api.build_zip(Path('release.zip'), True)
        finally:
            os.chdir(previous)


if __name__ == '__main__':
    unittest.main()
