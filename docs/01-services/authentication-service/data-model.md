# Data Model

Authentication Service owns one table, in its own MySQL database
([ADR-0008](../../00-infrastructure/adr/0008-mysql-as-database-engine.md)) — no other service
reads or writes it directly.

## `users`

| Field | Type | Notes |
|---|---|---|
| `id` | identifier (PK) | Generated at registration. This **is** the canonical user ID used system-wide — it's what gets published to Kafka for UserProfile to key its profile record on, and what goes inside the JWT. |
| `email` | string, unique | The login identity. Registration is rejected if this already exists. |
| `password_hash` | string | A bcrypt hash — the plaintext password is never stored, never logged. |
| `created_at` | timestamp | When the account was created. |

That's it for this phase — no `status`, `roles`, or session/token tables. There's no
role/permission distinction anywhere in the case study (every Registered User has the same
access), and logout/session handling doesn't need server-side state — see
[`security.md`](./security.md).

## Not modeled yet

Password reset (deferred — see [`backlog.md`](./backlog.md)) will need somewhere to store a
reset token/code and its expiry when it's built. Not added now to avoid documenting fields for a
flow that doesn't exist yet.
