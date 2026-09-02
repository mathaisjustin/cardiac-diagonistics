# Messaging

Bookmark Service is a **consumer only** — it never publishes. It has no direct connection to
Diagnosis Service at all (see that service's own
[`messaging.md`](../diagnosis-service/messaging.md) for why this replaced an earlier
direct-call design).

## What it consumes

**Topic**: `bookmark.created` · **Consumer group**: `bookmark-service`

**Payload** (`BookmarkEvent`, raw JSON string manually parsed via Jackson `ObjectMapper`):

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

**Validation**: a message missing `userId`, `diagnosisId`, or `payload` throws
`InvalidBookmarkEventException`.

**On receiving it**:
1. Checks `findByUserIdAndDiagnosisId(userId, diagnosisId)` — **idempotent**; if a bookmark
   already exists, logs and no-ops.
2. Otherwise creates a new `Bookmark` document (own generated id, copying `gender/age/bp/
   painType/treatment` from `payload`) and saves it.
3. Calls `bookmarkCacheService.evict(userId)` — invalidates the Redis cache so the next
   `GET /bookmarks` reflects the new bookmark. The cache is **not** populated here; it's only
   ever written on a `GET /bookmarks` cache miss.

**Error handling**: a DB save failure is wrapped as `BookmarkPersistenceException`.

## If this service is down when Diagnosis Service publishes

Nothing is lost — the event sits in the topic until this service is back up and consumes it.
Diagnosis Service's `POST /diagnosis/{id}/bookmark` already returned `202` to the client the
moment the message reached Kafka; it does not wait for this consumer to run.
