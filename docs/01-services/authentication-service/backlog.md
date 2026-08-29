# Deferred

Things this service will eventually need, deliberately not built or documented in detail yet —
tracked here so they aren't forgotten, without blocking what's actually in scope now.

## Password reset (US-03)

> As a Registered User, I want to reset my password so that I can regain access if I forget it.

Not built in this phase — only registration and login are. Deferring it also sidesteps a real
open question worth flagging now: the backlog says "request a reset link or code via registered
email," but there's no email/notification service anywhere in the current architecture (see
[`docs/ARCHITECTURE.md`](../../ARCHITECTURE.md)). When this gets picked up, that has to be
decided first — most likely Authentication sends the email itself via SMTP, rather than standing
up a whole separate Notification Service for one feature, but that's a decision for when we get
here, not now.

Will need, once built:
- A way to store a reset token/code + its expiry (not in `data-model.md` yet — see the note
  there).
- `POST /auth/password-reset/request` and `POST /auth/password-reset/confirm` (or similar) added
  to [`api-contract.md`](./api-contract.md).
- A decision on email delivery.
