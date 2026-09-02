# Security

## Password hashing

Passwords are hashed with **bcrypt**, via Spring Security's `PasswordEncoder`. The plaintext
password exists only for the instant it's being checked (login) or hashed (registration) — it is
never stored, logged, or put in a token.

**Password policy** (registration rejects anything weaker), precisely: **8–72 characters**, at
least one letter and one number. As a regex: `^(?=.*[A-Za-z])(?=.*\d).{8,72}$`. This is a
starting default, not a locked decision — easy to tighten later without affecting anything else
in this doc set.

**Why 72 is the upper bound, not an arbitrary round number**: bcrypt silently truncates anything
past 72 *bytes* — a 100-character password and its first-72-bytes prefix would hash identically,
which is confusing and worth avoiding outright rather than letting someone set a password that's
silently weaker than they think. Enforcing 72 as a hard max at validation time means what the
user typed is exactly what gets hashed, nothing truncated. On `POST /auth/register`, a password
outside this range returns `400 VALIDATION_ERROR` with `fields.password` explaining why — see
[`api-contract.md`](./api-contract.md).

## JWT contents

The token carries only what's needed to identify who's making a request:

| Claim | Value |
|---|---|
| `userId` | The user's ID from the `users` table. |
| `email` | The user's email. |
| `iat` / `exp` | Issued-at and expiry timestamps. |

**The password is never in the token.** A JWT's payload is base64-encoded, not encrypted —
anything inside it is readable by the browser, dev tools, and anything the token passes through.
The password is checked once, at login, against the bcrypt hash — after that it's not needed
again, which is the entire point of issuing a token in the first place.

## Signing and validation

Authentication signs the token with a secret key shared with the API Gateway. The Gateway
validates every token itself — signature and expiry — without calling back to Authentication
(see [ADR-0005](../../00-infrastructure/adr/0005-stateless-jwt-validation-at-gateway.md)).
Authentication only needs to *issue* tokens; it doesn't need to be up for the Gateway to keep
validating existing ones.

## Expiry

**60 minutes.** No refresh token — when it expires, the user logs in again. This directly
satisfies the case study's "inactive sessions time out automatically and require re-login."

## Logout

Logout is **client-side only**: the frontend deletes the token from local storage. There is no
server-side blocklist or session table.

This means a token that was "logged out" is still technically valid, cryptographically, until
its natural expiry — but the 60-minute expiry keeps that window small, and it means the Gateway's
validation stays fully stateless (no lookup against a blocklist on every request), which is the
whole reason ADR-0005 chose stateless validation in the first place. If this project ever needs
stronger guarantees (e.g. "logout must immediately invalidate everywhere"), that would need a
server-side blocklist — deliberately not building that now.

## Out of scope for this phase

Password reset — see [`BACKLOG.md`](../../BACKLOG.md).
