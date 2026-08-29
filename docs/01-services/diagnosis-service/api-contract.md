# API Contract

Four routes reached via the API Gateway, plus one internal route Bookmark Service calls
directly. Per the Gateway's
[route protection map](../../00-infrastructure/api-gateway/README.md#route-protection-map).
Error shape and status codes follow [api-conventions](../../03-cross-cutting/api-conventions.md).

| Route | Protection |
|---|---|
| `GET /diagnosis` | Public |
| `GET /diagnosis/{id}` | Public |
| `GET /diagnosis/search` | **Protected** |
| `GET /diagnosis/analysis` | **Protected** |

## The record shape

Every route below returns records in this shape (detail view) or a subset of it (list view):

```json
{
  "id": "1",
  "gender": "Male",
  "age": 45,
  "bp": "130/85",
  "painType": "Typical Angina",
  "cholesterol": 233,
  "diabetic": true,
  "smoker": false,
  "treatment": "Medication"
}
```

`id` is whatever the external Diagnosis API assigns — this service doesn't generate or reshape
it, just passes it through (relevant since it's also what Bookmark Service stores as
`diagnosisRecordId`).

## `GET /diagnosis`

Returns the full list of diagnosis records, fetched live from the external Diagnosis API.

**Behavior**: no filtering server-side. Basic browsing/searching for Guest and Registered Users
alike is done by the **frontend filtering this full list client-side** — deliberately not a
backend route, since it doesn't need to be (US-04's "loading state" / "error state" acceptance
criteria apply to this call).

**Success — `200 OK`**: array of records, **list-view fields only** — `id`, `gender`, `age`,
`bp`, `painType`, `treatment` (US-04's list view; `cholesterol`/`diabetic`/`smoker` are detail-
only, not sent here to keep the list payload smaller).

**Errors**

| Status | Code | When |
|---|---|---|
| `503` | `EXTERNAL_API_UNAVAILABLE` | The external Diagnosis API didn't respond. |

## `GET /diagnosis/{id}`

Returns full detail for one record (US-04: "clicking a record shows full details including
cholesterol, diabetic status and smoking status").

**Success — `200 OK`**: the full record shape above.

**Also used internally**: this is the same route Bookmark Service calls directly to confirm a
record exists before saving a bookmark reference to it — see [`flows.md`](./flows.md).

**Errors**

| Status | Code | When |
|---|---|---|
| `404` | `RECORD_NOT_FOUND` | No record with that ID. This is what Bookmark Service's direct call checks for. |
| `503` | `EXTERNAL_API_UNAVAILABLE` | The external Diagnosis API didn't respond. |

## `GET /diagnosis/search?painType=&age=&bp=&gender=` — Protected

Advanced search across the full dataset (US-05), any combination of the four fields.

**Request**: query params, all optional individually but **at least one required** — an empty
search is just browsing, which is what `GET /diagnosis` is for.

**Behavior**: fetches from the external API (filtered via its own query params where it supports
combining them, otherwise the full set filtered here) and returns matches.

**Success — `200 OK`**: array of matching records (list-view fields, same as `GET /diagnosis`).
An empty array is a **normal response**, not an error (US-05: "a clear 'no results' message
shows when nothing matches" is a frontend concern given an empty array).

**Errors**

| Status | Code | When |
|---|---|---|
| `400` | `VALIDATION_ERROR` | No filter provided at all — `fields` explains at least one of `painType`/`age`/`bp`/`gender` is required. |
| `503` | `EXTERNAL_API_UNAVAILABLE` | The external Diagnosis API didn't respond. |

## `GET /diagnosis/analysis?by=age|gender|painType` — Protected

Treatment-recommendation breakdown by one characteristic at a time (US-06).

**Request**: `by` — required, must be exactly one of `age`, `gender`, `painType`.

**Behavior**: fetches the **entire** dataset from the external API (not just current search
results — explicit US-06 acceptance criteria) and aggregates treatment counts grouped by the
requested characteristic, computed in-memory on each call (no caching — see
[ADR-0003](../../00-infrastructure/adr/0003-diagnosis-service-stateless-no-db.md), this dataset
is small enough that recomputing per request is fine).

**Success — `200 OK`**

```json
{
  "characteristic": "age",
  "breakdown": [
    { "value": "40-49", "treatments": { "Medication": 12, "Surgery": 3 } },
    { "value": "50-59", "treatments": { "Medication": 9, "Surgery": 7 } }
  ]
}
```

Exact bucketing for `age` (e.g. by decade, as above) is a frontend/display decision, not fixed
here yet — `gender` and `painType` group by their existing discrete values directly, no
bucketing needed.

**Errors**

| Status | Code | When |
|---|---|---|
| `400` | `VALIDATION_ERROR` | `by` missing, or not one of the three allowed values. |
| `503` | `EXTERNAL_API_UNAVAILABLE` | The external Diagnosis API didn't respond. |
