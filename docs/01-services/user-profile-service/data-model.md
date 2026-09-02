# Data Model

One table, `profiles`, in its own MySQL database `profiles_db`. Every row is created by the Kafka
consumer (see [`messaging.md`](./messaging.md)) — never by an API call.

| Column | Type | Notes |
|---|---|---|
| `user_id` | `VARCHAR`, PK | Set directly from the Kafka event's `userId` — the same UUID string Authentication generated at registration. Not auto-generated here; not updatable. |
| `profile_id` | `VARCHAR`, unique, not null | A separate 12-character generated ID (`UUID.randomUUID()`, first 12 hex chars, dashes stripped), assigned in `@PrePersist`. Not exposed by any current endpoint. |
| `first_name` | `VARCHAR`, not null | Editable via `PUT /profile`. |
| `last_name` | `VARCHAR`, not null | Editable via `PUT /profile`. |
| `contact` | `VARCHAR`, nullable | Editable via `PUT /profile`. Sourced from the Kafka event's `contactNumber` field. |
| `department` | `VARCHAR`, nullable | Editable via `PUT /profile`. |
| `created_at` | `TIMESTAMP`, not null | Set in `@PrePersist` when the Kafka event is consumed. |
| `updated_at` | `TIMESTAMP`, not null | Set in `@PrePersist`/`@PreUpdate`. |

**No `email` column.** Every response echoes the caller's `X-User-Email` header value straight
back, never persisted — see [ADR-0013](../../00-infrastructure/adr/0013-email-never-duplicated-into-userprofile.md).

## What's editable vs. not

`first_name`, `last_name`, `contact`, `department` can change via `PUT /profile`. `user_id` is
immutable; `email` isn't stored here at all.
