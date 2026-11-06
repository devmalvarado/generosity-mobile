# Public center and need discovery

The center/need slice was first exercised on 2026-11-03 and revised into the
map-based `/ayudar/` route on 2026-11-05. It is not deployed. The focused tasks
in section 7 of `add-web-participation-and-sharing` have local evidence; broader
participation and real HTTPS/session acceptance remain pending.

## What the page does

The help action opens `/ayudar/`; legacy hashes redirect there. Direct records
use `#centro/{id}` and `#necesidad/{id}`. The directory reads the existing bounded
`GET /api/v1/sync/catalog?category_version=2&progress_version=1` snapshot, with
credential-free public requests. Record details still use `GET /api/v1/centers/{id}`
and `GET /api/v1/needs/{id}`. A center links to its published needs, and an offer
form is shown only after opening a need. Server capacity validation remains final.

Home does not fetch the catalog. Invalid record ids do not fetch. A direct 404
stays missing. Catalog failure clears results/markers, preserves search and offers
Refresh; illustrative landing cards are never substituted. List pagination is
local (12 records) and search covers the entire snapshot, including addresses.
The API bounds snapshots to 2,000 centers / 10,000 needs; its 503 limit is handled
as unavailable, not as a silently truncated result.

Leaflet 1.9.4 and OSM tiles show only valid finite center coordinate pairs.
Invalid/missing locations remain accessible in the list. Pins open safe DOM
popups; list actions select the corresponding marker. The list has a keyboard
shortcut so users need not tab through every marker. Location is requested only
on a button press, uses a 10-second timeout, sorts all centers by straight-line
distance and never sends visitor coordinates to the API or browser storage.
OSM receives tile-region requests and connection metadata. Denial, timeout, missing
Leaflet and tile failure leave manual search and center links available.

Rendering uses text, not HTML, and allowlists localized public text/contact data.
Household, roster, recipient and user ids are not rendered. Coordinates drive
markers but are not printed in cards or share exports. Only `officially_verified`
produces the verification mark; a different arbitrary status cannot mint a badge.

Hosted sign-in remains reachable when session checks fail, opens a separate tab
and retains the draft. It never submits automatically. The loopback preview relay
reads only public catalog/detail paths, never sessions or writes. Local previews
state that account access happens on the published site; real HTTPS cookie/CORS
acceptance remains a release check.

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

## Map and access follow-up — 2026-11-05

Added Chromium/WebKit cases for optional location and distance sorting across more
than one page; accent-insensitive address search; missing/invalid coordinates;
pin/list selection; denied/timed-out location; unavailable renderer; hosted access
after a session failure; center → need → draft; and public-only local preview.
Existing CSRF, idempotency, capacity, account-switch, history/cancellation and
private-field assertions are retained. Automated tests stub OSM tiles.

Manual local review loaded all **171 public centers** through the relay and real
OSM cartography for Aguascalientes. No account was created, no live offer was sent,
and the user's physical location was not requested. No hosting changes were made.

Final automated result: **135 passed, 1 skipped** (the existing duplicate WebKit
screenshot). The later cross-page marker-selection assertion also passes in both
browsers after waiting for the popup fade transition. Strict validation passes
for both affected OpenSpec changes; the generated-brand check and preview package
self-check pass. The local relay returns 404 for private API/file paths and rejects
POST requests. The real hosted sign-in link opens the published page with its
login/register controls; no credentials, consent or live submission was used.
