# Data Model

Authentication Service owns one table, in its own MySQL database
([ADR-0008](../../00-infrastructure/adr/0008-mysql-as-database-engine.md)) — no other service
reads or writes it directly.

## `users`

| Field | Type | Notes |
|---|---|---|
| `id` | identifier (PK) | Generated at registration. This **is** the canonical user ID used system-wide — it's what gets published to Kafka for UserProfile to key its profile record on, and what goes inside the JWT. |
| `email` | `VARCHAR(255)`, unique | The login identity. Registration is rejected if this already exists. 255 is the standard practical max for an email address. |
| `password_hash` | `VARCHAR(60)` | A bcrypt hash — always exactly 60 characters in bcrypt's standard encoded form, regardless of password length. The plaintext password is never stored, never logged. |
| `created_at` | timestamp | When the account was created. |

That's it for this phase — no `status`, `roles`, or session/token tables. There's no
role/permission distinction anywhere in the case study (every Registered User has the same
access), and logout/session handling doesn't need server-side state — see
[`security.md`](./security.md).

**Deliberately not stored here**: first name, last name, phone number. Those are collected at
registration (see [`api-contract.md`](./api-contract.md)) but belong to UserProfile Service, not
Authentication — they're published to Kafka and never touch this table. This is why
[`messaging.md`](./messaging.md) and
[ADR-0011](../../00-infrastructure/adr/0011-registration-waits-for-kafka-producer-ack.md) treat
that Kafka publish so carefully: this table has no fallback copy of that data if the publish
were ever lost.

## Not modeled yet

Password reset (deferred — see [`BACKLOG.md`](../../BACKLOG.md)) will need somewhere to store a
reset token/code and its expiry when it's built. Not added now to avoid documenting fields for a
flow that doesn't exist yet.
