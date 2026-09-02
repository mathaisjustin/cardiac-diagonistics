# API Contract

Base path: `/bookmarks`, port `8082`. Both routes require header `X-User-Id` (trusted as-is, no
token validation). Neither route takes a user ID as a path/query param — a caller only ever acts
on the identity in their own header, but note this also means **anyone who can set the header can
read or delete any user's bookmarks** — see the trust-model caveat below.

## The bookmark shape

```json
{
  "id": "3fa8e9c1-...",
  "diagnosisId": "af5b",
  "gender": "Female",
  "age": 55,
  "bp": "140",
  "painType": "Atypical Angina",
  "treatment": "Lifestyle Changes",
  "createdAt": "2026-09-02T10:30:00"
}
```

`id` is the bookmark's own MongoDB `_id` (a freshly generated UUID, unrelated to `diagnosisId`).

## `GET /bookmarks`

**Request**: `X-User-Id` header only.

**Behavior**: reads from the Redis cache if present (key `bookmarks:<userId>`, 5-minute TTL); on
a miss, reads from MongoDB and populates the cache.

**Success — `200 OK`**: array of bookmarks belonging to that `userId` (empty array if none).

**Errors**: `400` if `X-User-Id` missing; `500` on a DB error.

## `DELETE /bookmarks/{id}`

`{id}` is the **bookmark's own** id, not the diagnosis record's.

**Behavior**: deletes only if `(id, userId)` both match — a request for the right bookmark id but
the wrong `userId` header returns `404`, **not** `403`, so a caller can't distinguish "doesn't
exist" from "belongs to someone else." Invalidates the Redis cache for that `userId` on success.

**Success — `200 OK`**, empty body.

**Errors**

| Status | When |
|---|---|
| `404` | No bookmark with that id belonging to this `userId`. |
| `400` | `X-User-Id` missing. |
| `500` | DB error, or any other unhandled exception. |

## Trust-model caveat

There's no API Gateway yet and this service does no token validation of its own — it takes
whatever `userId` is in the `X-User-Id` header at face value. Until a Gateway sits in front of
this service and forwards a header it has itself verified from a signed JWT (per
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)), do not
expose this port to untrusted clients.

## Error response shape

`{ timestamp, status, error, message }`.
