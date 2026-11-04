# Public center and need discovery

A partial landing slice was exercised locally on 2026-11-03. It is not deployed
and it does not complete any task in
`openspec/changes/add-web-participation-and-sharing`. Browser fixtures are not
a production HTTPS/CORS round trip.

## What the page does

The help action and the cause-card actions open `#descubrir`. That hash reads
existing `GET /api/v1/centers` and `GET /api/v1/needs`. Direct records use
`#centro/{id}` and `#necesidad/{id}`. The API origin and channel come from the
page meta tags. Public reads use credentials and `cache: no-store`, and they do
not mutate.

Home does not fetch the catalog. An invalid `#centro/` or `#necesidad/` prefix
does not fetch. A direct 404 stays missing and does not open another record.
If either list request fails, the results are cleared, the search text is kept,
and the illustration cards in `#necesidades` are not presented as current needs.
Those cards remain labeled "Illustration, not the published catalog" /
"Ilustración, no es el catálogo". An empty published page is stated as empty.

Rendering uses text, not HTML. It allowlists localized name, title, description
and address; string categories; a validated phone, email and HTTPS website;
quantities; expiry; and `updatedAt`. It does not render coordinates, household
or roster fields, recipient or user ids, or a verification badge.

A goods or time offer is prepared only against the open published need. Submit
requires a verified session, CSRF, a UUID `Idempotency-Key`, and
`contributions.create` from the matching feature snapshot. A lost response keeps
the same key and body. `capacity_exceeded` shows an integer `details.available`
only and does not keep that key for retry. Money has no input and stays
disabled. An expired need cannot be submitted. Signing in can restore a draft;
it does not submit it.

A verified session can read `GET /api/v1/me/contributions` and cancel its own
offered or confirmed row with `{ action: "cancel", version }`. Success is taken
from the refreshed row only when its status is `cancelled`. Switching accounts
clears owner-scoped drafts and history. A closed or failed
`contributions.create` snapshot blocks a new offer and does not undo a
cancellation already shown.

## Link ownership

The canonical invitation is the current landing origin, path and query, plus
`#centro/{id}` or `#necesidad/{id}`. Center ids match
`^[a-z0-9][a-z0-9-]{0,99}$`. Need ids match
`^[A-Za-z0-9][A-Za-z0-9-]{0,99}$`.

The landing does not mint a short link, QR image, campaign code or attribution
parameter. The visible readonly field is the source for copy, share and
download. Closing the share sheet records no send and makes no request. The
text file is `generosity-invitation.txt`. It names the record, the link and the
download date, and it says that it is not a scannable image and does not show
remaining places.

The deployed landing owns the hash. The API owns the published record. A hash
is not a reservation, grant or proof of delivery.

## Reproducible checks

From `portal/`: `npm test`.

On 2026-11-03 the design-token check passed and Playwright reported **111
passed, 1 skipped**. The skip is the existing WebKit screenshot duplicate in
`tests/portal.spec.js`. Asset queries were `portal.js?v=18251e764c02` and
`styles.css?v=284ecc4d1171`.

`tests/public-discovery.spec.js` ran 14 scenarios on Chromium and WebKit, all
against intercepted fixtures:

- help opens `#descubrir` without a POST or `/kindness-center`;
- home does not fetch the catalog;
- catalog failure keeps the search and clears results;
- a direct 404 and an invalid id do not open another record;
- a guest can prepare an offer and cannot submit it;
- money and an expired need stay disabled;
- a verified goods offer sends CSRF and retries the same key and body;
- `capacity_exceeded` does not record the offer;
- a restored draft is not submitted;
- the owner can cancel, and an account switch clears that draft;
- closing the share sheet records nothing, and the text file is not an image;
- 320px does not overflow and the submit control remains at least 44px;
- a closed or failed `contributions.create` snapshot blocks a new offer and
  leaves an existing cancellation in place.

The goods fixture checked quantity, notes, CSRF and the idempotency key. It did
not submit a time body. Copy is present; the suite did not click it. No native
client raced the same seat.

## Still unavailable

Provider login, payments, campaigns, meals, events, personal help, PWA/service
worker, assisted participation, inbox, preferences, outcome metrics, channel
attribution, analytics, QR images, native app links and reviewer selection are
not part of this browser slice. The subsequent [participation API increment](participation-api.md)
records partial local work for sections 4–6 on 2026-11-04; it does not supply the
missing browser/native journeys or deployment. No complete task was checked.
