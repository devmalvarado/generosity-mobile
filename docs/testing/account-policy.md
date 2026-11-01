# Adult eligibility, legal pages and permanent access block

2026-11-01 — policy version `2026-11-01`.

| Case | Automated evidence | Expected result |
| --- | --- | --- |
| Missing, declined or stale policy receipt | `backend/tests/Feature/AccountPolicyTest.php` | 422, no account or mail; valid receipt stores server timestamps |
| Social entry without declarations | Same suite | No redirect/attempt until policy validated; no password copied into attempt |
| Operator closure confirmation and replay | Same suite | Explicit operator/confirmation required; one audit, retained flag, credentials revoked |
| Password/token/reset/verification/re-registration | Same suite | Closed account never regains access; no duplicate account or queued email |
| Old browser session / provider subject / new subject with retained email | Same suite | Guest session or rejection, never reactivation |
| ES/EN public documents, invalid paths/locales | Same suite | Correct current document/contact, security headers, invalid routes rejected |
| Legal links, unchecked required declarations, narrow viewport and enlarged text | `portal/tests-auth/account-forms.spec.js` | Chromium/WebKit: 320px and 200% text, correct locale and no horizontal overflow |
| Landing contact, legal links and language changes | `portal/tests/portal.spec.js` | Working temporary mailto; API legal URLs follow current language |

Backend full suite: 57 tests / 710 assertions passed before adding the stale-session
case; the expanded six-case account-policy suite then passed all 81 assertions.
Portal: 67 passed, one intentional duplicate-screenshot skip. Hosted auth forms:
10 passed. OpenSpec: 16 items passed strict validation. PHP tests use only
`generosity_test`; browser auth checks use local Docker and submit invalid forms.

No production user is closed to validate this release. Google/Apple credentials
are still absent, so provider validation uses signed fixtures rather than live
accounts. Full erasure/provider revocation/native self-service deletion and
physical-device journeys remain pending. See the release record for production
publication and read-only checks, and `docs/legal/privacy-operations.md` for staff
handling of rights requests.


Production release `bd5b82e`: both deployment workflows succeeded. All ES/EN
legal URLs return 200 with version/contact/identity, hosted registration contains
all four policy fields, private intake is 401, private files remain inaccessible,
and the landing source hashes match the release. See the dated release record.
No production account was closed or created for these read-only checks.
