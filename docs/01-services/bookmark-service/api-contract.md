# API Contract

All three routes are **protected** — per the Gateway's
[route protection map](../../00-infrastructure/api-gateway/README.md#route-protection-map), a
valid JWT is required (US-07: "Guests are prompted to log in or register if they try to
bookmark"). Identity comes from `X-User-Id`, forwarded by the Gateway (see
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)) — no route
takes a user ID as a parameter, callers only ever act on their own bookmarks.

## `POST /bookmarks`

Bookmarks a diagnosis record.

**Request**: `diagnosisRecordId`.

**Behavior**:
- If this user already bookmarked this record, returns success without creating a duplicate row
  (see [`data-model.md`](./data-model.md)) — idempotent, not an error.
- Otherwise, calls Diagnosis Service directly (`GET /diagnosis/{id}`) to confirm the record
  exists and get its display fields (see [`messaging.md`](./messaging.md)).
  - Record not found → error, nothing saved.
  - Record found → saves a snapshot (see [`data-model.md`](./data-model.md);
    [ADR-0014](../../00-infrastructure/adr/0014-bookmark-stores-snapshot-not-reference.md) for
    why it's a snapshot, not a reference) and invalidates this user's cached bookmark list.

**Response**: the created bookmark.

## `GET /bookmarks`

Lists the caller's own bookmarks (US-08).

**Behavior**: reads from the Redis cache if present; on a cache miss, reads from MySQL and
populates the cache. Entirely self-contained — never calls Diagnosis Service.

**Response**: array of bookmarks (empty array if none — US-08: "an empty state shows when there
are no bookmarks yet," a frontend concern given an empty array, not a special API response).

## `DELETE /bookmarks/{id}`

Removes a bookmark (US-08: "users can remove a bookmark and it disappears from the list
immediately").

**Behavior**: deletes the row (scoped to the caller's own `user_id` — a user can't delete
someone else's bookmark by guessing an ID) and invalidates this user's cached bookmark list.

**Response**: success confirmation.
