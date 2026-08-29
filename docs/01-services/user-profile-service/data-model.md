# Data Model

One table, in its own MySQL database. Every row is created by the Kafka consumer (see
[`messaging.md`](./messaging.md)) — never by an API call.

## `profiles`

| Field | Type | Notes |
|---|---|---|
| `user_id` | identifier (PK) | Same ID Authentication generated at registration — not a separate ID. This is what ties a profile record back to its account. |
| `first_name` | string | Editable via `PUT /profile`. |
| `last_name` | string | Editable via `PUT /profile`. |
| `phone` | string | Editable via `PUT /profile`. |
| `created_at` | timestamp | When the Kafka event was consumed and the row created. |
| `updated_at` | timestamp | Last time the user edited their profile. |

**No `email` column.** Every request this service receives already carries the caller's email as
a Gateway-forwarded `X-User-Email` header (per
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)) — read
live from the validated JWT, not stored. See
[ADR-0013](../../00-infrastructure/adr/0013-email-never-duplicated-into-userprofile.md) for why
a stored copy was deliberately rejected.

## What's editable vs. not

Only `first_name`, `last_name`, and `phone` can change via `PUT /profile` (US-09: "editable
fields are validated before saving"). `user_id` is never touched by this service, and `email`
isn't stored here at all — if a user ever needs to change their email, that's an Authentication
concern, not modeled here.
