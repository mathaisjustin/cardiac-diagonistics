# Data Model

Two tables, in this service's own MySQL database `auth_db` (auto-created on boot,
`ddl-auto: update`) — no other service reads or writes it directly.

## `users`

| Column | Type | Notes |
|---|---|---|
| `user_id` | `VARCHAR`, PK | A UUID string, generated in `@PrePersist` (`UUID.randomUUID().toString()`) — application-generated, not a DB auto-increment. This is the canonical user ID used system-wide: it's the JWT `sub` claim and the key published to Kafka for UserProfile to key its profile record on. |
| `email` | `VARCHAR`, unique, not null | The login identity. |
| `password_hash` | `VARCHAR`, not null | A BCrypt hash. Plaintext is never stored or logged. |
| `created_at` | `TIMESTAMP`, not null | Set at registration. |
| `updated_at` | `TIMESTAMP`, not null | Set at registration; **not** touched again on password change — changing a password does not update this column. |

First name, last name, contact number, and department are collected at registration but never
stored here — they're published to Kafka and belong to UserProfile Service. See
[`messaging.md`](./messaging.md).

## `refresh_tokens`

| Column | Type | Notes |
|---|---|---|
| `id` | `BIGINT`, PK, auto-increment | Standard DB-generated identity. |
| `user_id` | `VARCHAR`, unique, not null | FK to `users.user_id`. Unique — each user has **at most one** active row; a new login/refresh deletes the old row first. |
| `token_hash` | `VARCHAR`, unique, not null | SHA-256 hash of the raw token, Base64-encoded. The raw token (32 random bytes, base64url, no padding) is never stored — only its hash, so a DB leak doesn't leak usable tokens. |
| `expires_at` | `TIMESTAMP`, not null | `now + 7 days` at issuance. |
| `created_at` | `TIMESTAMP`, not null | |
| `revoked` | `BOOLEAN`, not null, default `false` | Set `true` on logout or on a successful password change. A revoked row is not deleted — it's kept and just fails validation on reuse. |

## Not modeled

No `roles`/`status` column — every registered user has identical access in this system. No
password-reset table — password reset (backlog) isn't built yet.
