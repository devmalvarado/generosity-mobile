# Public origin

Confirmed by the product owner on 2026-10-31: `https://generosity.global`.

The origin responded on that date. `https://generosity.global/privacy` and
`https://generosity.global/terms` returned 404. Those paths are not Terms or
Privacy destinations. The product owner reports the landing deployed, with selective main-branch publication.
The existing `origin/main` workflow was inspected on 2026-10-31 and remains
unchanged. API deployment at `https://api.generosity.global` is intended but
not completed by the social-authentication change. Verified HTTPS app-link
associations remain pending; native auth currently uses PKCE-bound custom schemes.

The single code source is `org.generosity.domain.PublicOrigin`. An explicit
blank override does not invent another host. This record does not complete
welcome legal acceptance or a hosting decision.
