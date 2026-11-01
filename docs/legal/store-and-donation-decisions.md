# Legal draft and payment release decisions

Reviewed 2026-11-01. The owner approved adult-only accounts and publication of the current ES/EN terms and privacy notice. This is not an independent legal opinion, store acceptance, a complete deletion flow or payment enablement.

## Confirmed scope

The user requested permanent loss of access after account deletion. The institute
would receive contributions in its existing Mexican BBVA account and later
distribute funds to other centers. It has no confirmed current SAT donee
authorization (absent or pending). The owner supplied the institute's fiscal
identity on 2026-11-01; see the [Spanish terms and privacy index](terms-and-conditions.es.md).

## Institute identity and contact responsibility

The owner-provided fiscal screenshot identifies **INSTITUTO DE DEBERES Y DERECHOS
HUMANOS**, capital regime **ASOCIACION CIVIL**, RFC **IDD190329L69**. The fiscal
address is Temixco 7, acceso 1er callejón, colonia Pedregal de Santo Domingo,
Coyoacán, Ciudad de México, C.P. 04369, México. This records the supplied identity;
it does not establish SAT donee authorization or independently verify the document.

The owner authorized **notificaciones@generosity.global** as the temporary
operational support, legal and privacy contact, handled by institute personnel.
SMTP receipt was confirmed by the owner. Separate `soporte@`, `legales@` and
`privacidad@` mailboxes are still deferred; do not advertise them as operational.

The canonical documents live in `backend/resources/legal/` and are served at
`/legal/terms` and `/legal/privacy` in ES/EN. New password and social registrations
require explicit adult declaration and versioned terms/privacy acknowledgement.
Existing accounts retain null acceptance fields; no retrospective consent is fabricated.

## Source constraints

- Apple requires an in-app deletion entry and associated-data removal, with
  justified exceptions; deactivation alone is insufficient. Sign in with Apple
  also requires token revocation. [Apple account deletion](https://developer.apple.com/help/app-review/guideline-reference/5-1-1-account-deletion).
- Google requires both an in-app path and an external web deletion resource;
  retained data needs a disclosed legitimate justification. [Google deletion](https://support.google.com/googleplay/android-developer/answer/13327111).
- Mexican private-sector data law distinguishes cancellation, blocking and
  suppression, with applicable exceptions. It does not justify keeping a complete
  usable account forever. [LFPDPPP, articles 24–25](https://www.diputados.gob.mx/LeyesBiblio/pdf/LFPDPPP.pdf).
- Apple clauses 3.2.1(vi)/3.2.2(iv) distinguish approved in-app nonprofit
  fundraising from outside-app collection. Clause 3.1.3(g) treats same-app paid
  promotion separately. [App Review Guidelines](https://developer.apple.com/app-store/review/guidelines/).
- Non-US nonprofit Apple Pay approval uses Benevity; payment processing still
  needs a provider. [Apple nonprofit onboarding](https://developer.apple.com/apple-pay/nonprofits/).
- Google excludes tax-exempt donations from Play Billing. This does not establish
  eligibility for this institute or its redistribution model. [Google payments](https://support.google.com/googleplay/android-developer/answer/9858738).
- SAT donor guidance requires eligible recipients and documentation for a
  deductible donation. Do not advertise deduction or issue deductible-donation
  receipts on the facts currently supplied. [SAT donations](https://www.sat.gob.mx/minisitio/DeduccionesPersonales/donaciones.html).
- BBVA describes bank transfers using CLABE and SPEI. This supports using
  a deposit account as a destination, not an assumption that this account includes
  a checkout API or third-party fundraising approval. [BBVA SPEI](https://www.bbva.mx/personas/servicios-digitales/transferencias-bancarias.html).

## Proposed product rules and implementation consequences

Deletion first commits an irreversible access block and revokes credentials;
asynchronous purging must not reopen the account on failure. Retain only
purpose-bound minimum records with an approved expiry, not a full soft-deleted
profile. Anonymized information must no longer identify a person; a reversible
alias is not sufficient. Reconnecting clients purge private caches and pending
writes. Backups and delayed identity callbacks must not resurrect access.

The requested money route is donor → institute → recipient center. It needs
separate collection, bank-settlement and distribution evidence. A donor payment
does not prove that the center received support. Use provider-hosted collection,
controlled organizational recipients and a private allocation/distribution
ledger; do not create a consumer wallet or automate bank transfers yet. No
BBVA password, bank authorization codes or card credentials belong in the app or repository.

Do not select a generic merchant account before disclosing onward distribution.
For example, Stripe distinguishes restricted nonprofit fundraising from other
charitable fundraising and crowdfunding, including country limitations. That is
an eligibility question, not evidence that Stripe supports this case.
[Stripe restricted businesses](https://stripe.com/en-mx/legal/restricted-businesses).

SAT authorization, nonprofit status, payment-provider underwriting and store
approval are separate determinations. Sending money onward to an authorized
donee does not establish that the donor's payment to the institute is deductible.
Ask Mexican counsel/accounting reviewers to classify the actual flow, examine
statutes, beneficiary agreements, accounting and any applicable reporting duties.
Do not describe the institute as merely a neutral directory when it handles funds.

## Release decisions still open

- Arrange ongoing institute-staff coverage of the temporary mailbox; activate and verify separate mailboxes before replacing it. Independent legal review remains pending.
- Approve per-category retention, deletion completion time, backup expiry and
  exception review. Build and test the deletion paths before publishing promises.
- Approve institute-to-center agreements, beneficiary onboarding, restricted-use
  allocations, fees, delivery timeframes, failed campaigns, refunds and disputes.
- Obtain written provider acceptance of the Mexican collection/redistribution
  model and confirm BBVA account holder, currency and settlement compatibility.
- Record store eligibility per platform/territory and fundraiser/recipient. Until
  resolved, monetary entry and links remain disabled in native apps. Apple’s
  outside-app route does not automatically authorize the Android equivalent.
- Complete self-service deletion pages and report actual behavior in store disclosures. Current terms/privacy and versioned registration acceptance are separate from full deletion. Commercial
  listings, campaigns and advertising need their own approved purchase rules.

Implementation remains tracked in foundation tasks 20.1–20.8 and reference
product tasks 10.1–10.11. No payment, fiscal receipt or deletion task is complete
merely because the current terms/privacy are published. The owner explicitly deferred full erasure and native deletion UI; the scoped access-block release is tracked separately in tasks 20.9–20.11.
