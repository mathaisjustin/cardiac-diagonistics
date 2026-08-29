# API Contract

Four routes reached via the API Gateway, plus one internal route Bookmark Service calls
directly. Per the Gateway's
[route protection map](../../00-infrastructure/api-gateway/README.md#route-protection-map):

| Route | Protection |
|---|---|
| `GET /diagnosis` | Public |
| `GET /diagnosis/{id}` | Public |
| `GET /diagnosis/search` | **Protected** |
| `GET /diagnosis/analysis` | **Protected** |

## `GET /diagnosis`

Returns the full list of diagnosis records, fetched live from the external Diagnosis API.

**Behavior**: no filtering server-side. Basic browsing/searching for Guest and Registered Users
alike is done by the **frontend filtering this full list client-side** — deliberately not a
backend route, since it doesn't need to be (US-04's "loading state" / "error state" acceptance
criteria apply to this call).

**Response**: array of records — gender, age, bp, pain type, treatment (list view fields per
US-04).

## `GET /diagnosis/{id}`

Returns full detail for one record (US-04: "clicking a record shows full details including
cholesterol, diabetic status and smoking status").

**Behavior**: fetches the single record from the external API. Returns 404 if the ID doesn't
exist.

**Also used internally**: this is the same route Bookmark Service calls directly to confirm a
record exists before saving a bookmark reference to it — see [`flows.md`](./flows.md).

## `GET /diagnosis/search?painType=&age=&bp=&gender=` — Protected

Advanced search across the full dataset (US-05), any combination of the four fields.

**Behavior**: fetches from the external API (filtered via its own query params where it supports
combining them, otherwise the full set filtered here) and returns matches. Empty result set is a
normal response, not an error (US-05: "a clear 'no results' message shows when nothing
matches" — that's a frontend concern, this route just returns an empty array).

**Response**: array of matching records.

## `GET /diagnosis/analysis?by=age|gender|painType` — Protected

Treatment-recommendation breakdown by one characteristic at a time (US-06).

**Behavior**: fetches the **entire** dataset from the external API (not just current search
results — explicit US-06 acceptance criteria) and aggregates treatment counts grouped by the
requested characteristic, computed in-memory on each call (no caching — see
[ADR-0003](../../00-infrastructure/adr/0003-diagnosis-service-stateless-no-db.md), this dataset
is small enough that recomputing per request is fine).

**Response**: grouped counts, e.g. `{ "characteristic": "age", "breakdown": [...] }` — exact
shape TBD when the frontend's analysis view is designed.
