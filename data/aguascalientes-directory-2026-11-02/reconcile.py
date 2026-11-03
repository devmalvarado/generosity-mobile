"""Read the original workbook; emit reviewed public data and a private import batch.

Usage: bundled-python reconcile.py workbook.xlsx current-directory-snapshot.json
The source workbook is never modified. Requires read-only openpyxl.
"""
import hashlib
import json
from pathlib import Path
import sys

import openpyxl

ROOT = Path(__file__).resolve().parent
workbook_path, snapshot_path = map(Path, sys.argv[1:3])
source_hash = hashlib.sha256(workbook_path.read_bytes()).hexdigest()
workbook = openpyxl.load_workbook(workbook_path, read_only=True, data_only=True)
current = {c['id']: c for c in json.loads(snapshot_path.read_text())}
decisions = json.loads((ROOT / 'decisions.json').read_text())
rows, report, centers = [], [], []

for sheet in ['Organizaciones', 'Comedores', 'Dispensario Médico']:
    for number, values in enumerate(workbook[sheet].iter_rows(values_only=True), 1):
        name_index = {'Organizaciones': 2, 'Comedores': 1, 'Dispensario Médico': 0}[sheet]
        if number == 1 or len(values) <= name_index or not values[name_index]:
            continue
        name = str(values[name_index]).strip().replace('\n', ' ')
        if sheet == 'Comedores' and values[2]:
            name += ' — ' + str(values[2]).strip()
        key = f'{sheet}:{number}'
        decision = decisions.get(key, {})
        center_id = decision.get('centerId')
        notes = decision.get('notes', [
            'Workbook declaration retained privately. No independently corroborated operational location and place pin in this review; further research is pending.'
        ])
        if not decision and sheet == 'Organizaciones' and not any(values[i] for i in [4, 11, 12]):
            notes.append('The workbook supplies no address, map URL or coordinates for this row. A name or tax-directory entry alone is insufficient to publish a location.')
        status = decision.get('status', 'pending')
        if 'center' in decision:
            center = decision['center'].copy()
            center_id = center['id']
            assert center_id not in current, f'New center already exists: {center_id}; reconcile before retrying.'
            center['expectedVersion'] = 0
            centers.append(center)
            status = 'applied'
        elif 'updates' in decision:
            assert center_id in current
            center = {k: v for k, v in current[center_id].items() if k not in ['version', 'updatedAt']}
            center.update(decision['updates'])
            center['expectedVersion'] = current[center_id]['version']
            center['needs'] = []  # Import preserves existing need records; no invented solicitation.
            centers.append(center)
            status = 'applied'
        if center_id and 'center' not in decision:
            assert center_id in current
        source_data = {openpyxl.utils.get_column_letter(i): str(v) if v is not None else None
                       for i, v in enumerate(values, 1) if v is not None}
        rows.append({'sheet': sheet, 'rowNumber': number, 'centerId': center_id,
                     'status': status, 'sourceData': source_data, 'reviewNotes': notes,
                     'sourceUrls': decision.get('sources', [])})
        report.append({'sheet': sheet, 'rowNumber': number, 'name': name, 'centerId': center_id,
                       'status': status, 'reviewNotes': notes, 'sourceUrls': decision.get('sources', [])})

assert len(rows) == 131, len(rows)
assert len({(r['sheet'], r['rowNumber']) for r in rows}) == len(rows)
assert len({c['id'] for c in centers}) == len(centers)
assert all(c['verificationStatus'] == 'imported' and not c['needs'] for c in centers)
assert set(decisions).issubset({f"{r['sheet']}:{r['rowNumber']}" for r in rows})

def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')

batch = {'schemaVersion': 1, 'centers': centers}
write(ROOT / 'catalog.json', batch)
write(ROOT / 'reconciliation.json', {
    'sourceName': workbook_path.name, 'sourceSha256': source_hash, 'reviewedOn': '2026-11-02',
    'excludedSheets': ['DB (empty)', 'Ideas (taxonomy/country notes, not center listings)'],
    'excludedRows': 'Organizaciones 112–124 contain numbering only; blank rows are not listings.',
    'rows': report,
})
batch['reviewBatch'] = {'sourceName': workbook_path.name, 'sourceSha256': source_hash, 'rows': rows}
private_path = ROOT.parents[1] / 'backend/storage/app/private/aguascalientes-directory-import.json'
write(private_path, batch)
private_path.chmod(0o600)
print(json.dumps({'sourceRows': len(rows), 'updates': sum(c['id'] in current for c in centers),
                  'new': sum(c['id'] not in current for c in centers),
                  'statuses': {s: sum(r['status'] == s for r in rows) for s in ['pending', 'applied', 'matched', 'duplicate']}}, indent=2))
