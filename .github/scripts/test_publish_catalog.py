import copy
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import publish_catalog


class CatalogPublicationTest(unittest.TestCase):
    def test_unconfirmed_action_stops_before_connecting(self):
        with patch.dict('os.environ', {}, clear=True), patch.object(publish_catalog, 'connect') as connect:
            with self.assertRaises(SystemExit):
                publish_catalog.main()
            connect.assert_not_called()

    def test_reviewed_batch_and_invalid_variants(self):
        original = json.loads(publish_catalog.reviewed_payload())
        research = (publish_catalog.BATCH / 'research.json').read_text()
        with tempfile.TemporaryDirectory() as directory, patch.object(publish_catalog, 'BATCH', Path(directory)):
            (Path(directory) / 'research.json').write_text(research)
            for field, value in [('needs', [{'id': 'invented'}]), ('verificationStatus', 'officially_verified'),
                                 ('id', 'unreviewed'), ('coordinates', {'latitude': 0, 'longitude': 0})]:
                document = copy.deepcopy(original)
                document['centers'][0][field] = value
                (Path(directory) / 'catalog.json').write_text(json.dumps(document))
                with self.assertRaises(ValueError):
                    publish_catalog.reviewed_payload()

    def test_release_keeps_existing_application_and_has_no_migration(self):
        script = publish_catalog.release_script('a' * 64, 'b' * 64, 'reviewed.json').decode()
        self.assertNotIn('{{', script)
        self.assertIn('hash_equals', script)
        self.assertIn('DB::transaction', script)
        self.assertIn('batch_ids_exist_review_before_retry', script)
        self.assertIn('before.json', script)
        self.assertIn('0600', script)
        self.assertIn('@unlink(__FILE__)', script)
        for forbidden in ['migrate', '.env', 'password', 'TRUNCATE', 'DELETE FROM', 'extractTo']:
            self.assertNotIn(forbidden, script)


if __name__ == '__main__':
    unittest.main()
