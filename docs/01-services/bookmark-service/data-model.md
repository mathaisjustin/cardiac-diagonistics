# Data Model

One collection, `bookmarks`, in MongoDB (`bookmark_db`). Every document is created by the Kafka
consumer (see [`messaging.md`](./messaging.md)) — never by an API call.

| Field | Type | Notes |
|---|---|---|
| `id` | `String` (`@Id`) | The bookmark's own id — a freshly generated `UUID.randomUUID().toString()`, unrelated to `diagnosisId`. |
| `userId` | `String` | Whose bookmark this is, from the Kafka event. |
| `diagnosisId` | `String` | The diagnosis record's id (from Diagnosis Service / the external API). |
| `gender` | `String` | Snapshot field, captured at bookmark-creation time. |
| `age` | `Integer` | Snapshot field. |
| `bp` | `String` | Snapshot field — stored as a string (matches how Diagnosis Service publishes it), not numeric. |
| `painType` | `String` | Snapshot field. |
| `treatment` | `String` | Snapshot field. |
| `createdAt` | `LocalDateTime` | Set when the document is created. |

## Uniqueness

**Compound unique index on `(userId, diagnosisId)`**, named `user_diagnosis_unique`. A user can't
bookmark the same record twice — the Kafka consumer checks `findByUserIdAndDiagnosisId` first and
skips (logs, no-op) rather than relying on the index to reject a duplicate insert.

## Snapshot, not a reference

These fields are exactly what Diagnosis Service put in the `bookmark.created` event — enough to
render a bookmarks list without ever calling Diagnosis Service again. Full record detail
(cholesterol, diabetic status, smoking status) isn't stored here; clicking into a bookmarked
record for that would be a normal `GET /diagnosis/{id}` call, same as browsing.
