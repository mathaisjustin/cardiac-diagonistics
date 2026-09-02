# Messaging

Diagnosis Service is a **publisher only** — it never consumes anything, including nothing back
from Bookmark Service. The two services communicate exclusively through Kafka, one direction.

This replaced an earlier idea of Bookmark Service calling `GET /diagnosis/{id}` directly to
validate a record before saving a reference to it. That direct-call design was dropped: it would
have made Bookmark Service depend on Diagnosis Service being up at bookmark-creation time, and it
put the "does this record exist" check in the wrong place — Diagnosis Service already has the
data live in hand at the moment the user clicks bookmark (`POST /diagnosis/{id}/bookmark`), so it
makes more sense for this service to resolve the record itself and hand a **complete snapshot**
to Bookmark Service, rather than Bookmark Service reaching back to ask.

## What it publishes

**Trigger**: `POST /diagnosis/{id}/bookmark`.

**Topic**: `bookmark.created` · **Key**: `userId`

**Payload** (`BookmarkEvent`, JSON string via Jackson):

```json
{
  "userId": "6f1a2b3c-...",
  "diagnosisId": "af5b",
  "payload": {
    "gender": "Female",
    "age": 55,
    "bp": "140",
    "painType": "Atypical Angina",
    "treatment": "Lifestyle Changes"
  }
}
```

Note `bp` is stringified (`String.valueOf`) even though the external API returns it as an int —
this matches Bookmark Service's own DTO shape exactly, field for field.

**Reliability**: the publish is synchronous (`.get()` on the Kafka future) — if it fails, the
route returns `503` immediately rather than reporting success on an event that never left the
service. There's no rollback needed here (unlike Authentication's registration), since this
service has no database state of its own to undo.

## Who consumes it

Bookmark Service only — see its [`messaging.md`](../bookmark-service/messaging.md).
