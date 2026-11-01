# Privacy and account-closure operations

Owner-approved contact: notificaciones@generosity.global, handled by institute
personnel. Keep mailbox credentials outside Git; limit access to designated staff.
The separate support/legal/privacy mailboxes are not yet operational.

## Intake and rights

Record receipt date, a private request reference, requested rights, reply channel,
verification status, responsible staff and deadline in the institute's restricted
case record. Acknowledge the request and verify identity/representation proportionally;
never request a password or OTP. A closure request and an ARCO cancellation request
are different actions. Process both when requested; do not require an active account.

For ARCO, communicate a determination within 20 business days and, where granted,
implement within the following 15 business days. Record any legally justified single
extension and communicate it. Consult the applicable law/counsel for exceptions,
identity clarification and disputes; do not convert every case into an exception.

Review account/profile, social identities, applications, contributions, visits,
audit/operation payloads and relevant logs/backups. Restrict retained records to
specific obligations and document the legal basis plus expiry/review criterion.
Do not retain a full identifiable profile indefinitely merely because it is flagged.
Fulfill correction/access/cancellation through an authorized reviewed operation;
never email an unrelated user's history or publish a production database dump.

## Permanent access block available in this release

After verifying the request and account ID, an authorized server operator runs:

```sh
php artisan account:deactivate ACCOUNT_ID --confirmed --operator=STAFF_REFERENCE
```

Use the production PHP 8.4 binary and private application directory documented in
`docs/hostinger-backend.md`. The command sets `users.deactivated_at`, replaces the
password, clears remember tokens and revokes API tokens, sessions, password resets,
native handoffs and pending provider-link attempts in one transaction. It is idempotent
and records the operator and account ID. Do not edit the database flag directly:
that would skip revocation. There is no supported reactivation command.

Retained social subjects/emails cannot silently create a replacement account.
Queued account mail is suppressed. The public wording says access is blocked when
the request is executed, not instantly when an email arrives. Offline device copies
cannot be remotely wiped until contact; do not describe the flag as erasure.

Full self-service deletion, per-category purge/expiry automation, provider revocation
and native cache cleanup remain tracked and unchecked in OpenSpec. Store deletion
requirements are not complete. Operator-assisted data cancellation must still be
handled independently under the published notice and applicable obligations.

## Recovery and publication

Before restoring a backup, keep the service closed and replay every subsequent
closure/cancellation from the restricted case record and operator audit. Verify
closed users cannot authenticate before reopening. Review backup retention and
restrict access; never restore an old flag value to reactivate a closed account.

Current policy version: `2026-11-01`. Acceptance is server-stamped for new accounts;
legacy accounts have null values and are not claimed to have accepted retrospectively.
Future material changes need an explicit migration/acceptance flow before use.
The project owner's publication approval is not a substitute for independent legal review.
