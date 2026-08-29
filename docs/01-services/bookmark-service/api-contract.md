# API Contract

All three routes are **protected** — per the Gateway's
[route protection map](../../00-infrastructure/api-gateway/README.md#route-protection-map), a
valid JWT is required (US-07: "Guests are prompted to log in or register if they try to
bookmark"). Identity comes from `X-User-Id`, forwarded by the Gateway (see
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)) — no route
takes a user ID as a parameter, callers only ever act on their own bookmarks. Error shape and
status codes follow [api-conventions](../../03-cross-cutting/api-conventions.md).

## The bookmark shape

```json
{
  "id": "b-1",
  "diagnosisRecordId": "1",
  "gender": "Male",
  "age": 45,
  "bp": "130/85",
  "painType": "Typical Angina",
  "treatment": "Medication",
  "createdAt": "2026-01-15T10:30:00Z"
}
```

The five snapshot fields are exactly Diagnosis Service's list-view fields (see its
[`api-contract.md`](../diagnosis-service/api-contract.md)) — nothing beyond what a bookmarks
list needs to display.

## `POST /bookmarks`

Bookmarks a diagnosis record.

**Request**

```json
{ "diagnosisRecordId": "1" }
```

**Behavior**:
- If this user already bookmarked this record, returns the existing bookmark without creating a
  duplicate row (see [`data-model.md`](./data-model.md)) — idempotent, `200` not `201`.
- Otherwise, calls Diagnosis Service directly (`GET /diagnosis/{id}`) to confirm the record
  exists and get its display fields (see [`messaging.md`](./messaging.md)).
  - Not found → error, nothing saved.
  - Found → saves a snapshot ([ADR-0014](../../00-infrastructure/adr/0014-bookmark-stores-snapshot-not-reference.md))
    and invalidates this user's cached bookmark list.

**Success**: `201 Created` (new bookmark) or `200 OK` (already existed) — both return the
bookmark shape above.

**Errors**

| Status | Code | When |
|---|---|---|
| `400` | `VALIDATION_ERROR` | `diagnosisRecordId` missing. |
| `404` | `RECORD_NOT_FOUND` | Diagnosis Service confirmed no record exists with that ID — same code Diagnosis Service itself uses, passed through rather than wrapped in something new. |
| `503` | `DIAGNOSIS_SERVICE_UNAVAILABLE` | The direct call to Diagnosis Service failed (service down, not reachable) — distinct from Diagnosis's own `EXTERNAL_API_UNAVAILABLE`, since this is a different failure point (Bookmark couldn't even reach Diagnosis, vs. Diagnosis reaching the external API and failing). |

## `GET /bookmarks`

Lists the caller's own bookmarks (US-08).

**Behavior**: reads from the Redis cache if present; on a cache miss, reads from MySQL and
populates the cache. Entirely self-contained — never calls Diagnosis Service.

**Success — `200 OK`**: array of bookmarks (empty array if none — US-08: "an empty state shows
when there are no bookmarks yet," a frontend concern given an empty array, not a special API
response). No errors specific to this route beyond the standard auth failures the Gateway already
handles.

## `DELETE /bookmarks/{id}`

Removes a bookmark (US-08: "users can remove a bookmark and it disappears from the list
immediately"). `{id}` is the **bookmark's** ID (from the shape above), not the diagnosis
record's.

**Behavior**: deletes the row, scoped to the caller's own `user_id` — a user can't delete
someone else's bookmark by guessing an ID — and invalidates this user's cached bookmark list.

**Success — `200 OK`**: confirmation.

**Errors**

| Status | Code | When |
|---|---|---|
| `404` | `BOOKMARK_NOT_FOUND` | No bookmark with that ID belonging to this user — returned identically whether the ID doesn't exist at all or belongs to someone else, so a caller can't use this to probe whether a given ID exists for another user. |
