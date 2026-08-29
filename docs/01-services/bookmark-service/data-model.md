# Data Model

One table, in its own MySQL database.

## `bookmarks`

| Field | Type | Notes |
|---|---|---|
| `id` | identifier (PK) | The bookmark's own ID (not the diagnosis record's). |
| `user_id` | identifier | From `X-User-Id` at creation time — whose bookmark this is. |
| `diagnosis_record_id` | `VARCHAR(50)` | The external Diagnosis API's record ID, stored as a string — links back to the full record if the user clicks through. Not assumed to be numeric, since it's controlled by an external system this project doesn't own. |
| `gender` | `VARCHAR(20)` | Snapshot field, captured at bookmark time. |
| `age` | `INT` | Snapshot field. |
| `bp` | `VARCHAR(20)` | Snapshot field. |
| `pain_type` | `VARCHAR(50)` | Snapshot field. |
| `treatment` | `VARCHAR(100)` | Snapshot field. |
| `created_at` | timestamp | When bookmarked. |

Snapshot fields match US-04's list-view fields — enough to show a useful bookmarks list without
calling Diagnosis Service. If a user clicks into a bookmarked record for full detail (cholesterol,
diabetic status, smoking status), that's a normal `GET /diagnosis/{id}` call at that point — same
as browsing normally, not part of viewing the bookmarks list itself.

## Uniqueness

**Unique constraint on (`user_id`, `diagnosis_record_id`)** — a user can't bookmark the same
record twice. See [`api-contract.md`](./api-contract.md) for how `POST /bookmarks` handles a
repeat bookmark attempt (no-op success, not an error or a duplicate row).
