# API Gateway — Route Reference

Every route reachable through the Gateway, how each one authenticates, and what to expect back.
Use this to reproduce or extend the Postman test plan.

**Base URL (use this for all testing): `http://localhost:9090`**
Direct service ports (`8080`–`8083`) exist only for the spoofing/security tests at the bottom —
don't use them for normal testing.

## How identity moves through the system

The Gateway is the **only** place a JWT is ever parsed. It verifies the token's signature and
expiry, then forwards identity downstream as `X-User-Id` (and `X-User-Email` where needed),
signed with an HMAC (`X-Identity-Signature`) over the shared secret — not a plain header anyone
could set. Each backend service checks that signature before trusting the header; it never
re-parses the JWT itself.

Any identity header a client sets manually is stripped by the Gateway before it signs its own —
there is no way to override `X-User-Id` by hand, even alongside a valid token. Hitting a service
directly on its own port with a forged header is rejected independently, with
`401 "Invalid identity signature"` — see the Security tests section.

---

## Authentication — `/api/auth`

Every route here is reachable without a token — `register`/`login` because you can't have a
token yet, `refresh`/`logout` because they operate on the refresh token in the body instead.
`change-password` is the one exception: it requires a valid access token, verified by
auth-service's own JWT filter (not the Gateway's signed-header model, since this service issues
the tokens in the first place).

### `POST /api/auth/register` — public

```json
{
  "email": "jane@example.com",
  "password": "Sw0rdfish1",
  "firstName": "Jane",
  "lastName": "Doe",
  "contactNumber": "555-0100",
  "department": "Cardiology"
}
```

| Status | When |
|---|---|
| `201` | Created, empty body |
| `400` | a field is blank |
| `409` | email already registered |
| `503` | Kafka publish failed, nothing saved |

### `POST /api/auth/login` — public

```json
{ "email": "jane@example.com", "password": "Sw0rdfish1" }
```

| Status | When |
|---|---|
| `200` | `{ accessToken, refreshToken }` |
| `401` | "Invalid email or password" — same message either way |

### `POST /api/auth/refresh` — public

```json
{ "refreshToken": "..." }
```

| Status | When |
|---|---|
| `200` | `{ accessToken, refreshToken }` (rotated) |
| `401` | not found, revoked, or expired |

### `POST /api/auth/logout` — public

```json
{ "refreshToken": "..." }
```

| Status | When |
|---|---|
| `204` | no body |
| `401` | token not found |

The access token already issued keeps working until its own 15-minute expiry — logout only
prevents it from being refreshed again.

### `POST /api/auth/change-password` — requires JWT

Headers: `Authorization: Bearer <accessToken>`

```json
{ "oldPassword": "...", "newPassword": "..." }
```

| Status | When |
|---|---|
| `200` | "Password changed successfully" — also revokes the caller's existing refresh token |
| `400` | old password incorrect |
| `401` | no/invalid/expired token |

---

## Profile — `/api/profile`

Both routes act on **the caller's own account only** — there is no route shaped like
`/profile/{userId}`. Identity comes exclusively from a verified, signed `X-User-Id` /
`X-User-Email` pair the Gateway attaches after checking the JWT.

### `GET /api/profile` — requires JWT

```json
{
  "email": "jane@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "contact": "555-0100",
  "department": "Cardiology"
}
```

| Status | When |
|---|---|
| `200` | profile found |
| `401` | missing/invalid token (rejected at the Gateway) |
| `404` | Kafka event not consumed yet (rare, transient) |

### `PUT /api/profile` — requires JWT

Full update — send all four editable fields, not just the changed ones.

```json
{ "firstName": "Jane", "lastName": "Doe", "contact": "555-0101", "department": "Radiology" }
```

| Status | When |
|---|---|
| `200` | updated profile |
| `400` | blank field or malformed JSON |
| `401` | missing/invalid token |

---

## Diagnosis — `/api/diagnosis`

Mixed access. The Gateway never blocks this path — it attaches a signed `X-User-Id` when a
valid token happens to be present and passes everything else through untouched.
Diagnosis-service itself decides, per route, whether that identity is required.

### `GET /api/diagnosis` — public

List view, trimmed to just enough to browse — same shape for guest and logged-in.

```json
{ "id": "b47d", "gender": "Male", "age": 70, "pain_type": "Typical Angina" }
```

| Status | When |
|---|---|
| `200` | array of records |
| `503` | external diagnosis API unreachable |

### `GET /api/diagnosis/{id}` — optional

Response shape depends on whether a valid token is present.

**Guest** — no `treatment`:
```json
{
  "id": "b47d", "gender": "Male", "age": 70, "bp": 181, "cholesterol": 262,
  "diabetic": "No", "smoking_status": "Never", "pain_type": "Typical Angina"
}
```

**Logged in** — adds `treatment`:
```json
{ "...same fields...": "...", "treatment": "Lifestyle Changes" }
```

| Status | When |
|---|---|
| `200` | record found |
| `404` | no record with that id |

### `GET /api/diagnosis/search` — requires JWT

Enforced by diagnosis-service itself, not the Gateway — missing identity gets a 401 from the
service, with a specific message.

Query params (at least one required): `?gender=Male&painType=Typical%20Angina&ageMin=40&ageMax=60&bpMin=120&bpMax=150`

| Status | When |
|---|---|
| `200` | matching records, full detail (possibly empty) |
| `400` | no filter given, or an invalid value |
| `401` | "Advanced search requires you to be logged in" |

### `GET /api/diagnosis/analysis` — requires JWT

Always runs against the full dataset — current search filters don't apply.

Query params: `?by=age` (or `gender` / `painType`)

| Status | When |
|---|---|
| `200` | counts/percentages by group + overall |
| `400` | `by` missing or invalid |
| `401` | "Treatment analysis requires you to be logged in" |

### `POST /api/diagnosis/{id}/bookmark` — requires JWT

Publishes a snapshot to Kafka for bookmark-service to consume — this route has no database of
its own and doesn't wait for the save to complete.

```json
{ "message": "Bookmark request submitted", "diagnosisId": "b47d" }
```

| Status | When |
|---|---|
| `202` | accepted, published to Kafka |
| `401` | "Bookmarking requires you to be logged in" |
| `404` | no record with that id |

---

## Bookmark — `/api/bookmarks`

No creation route here — a bookmark only ever comes to exist via the Kafka event Diagnosis
Service publishes. These two routes only read and remove, always scoped to the caller's own
`X-User-Id`.

### `GET /api/bookmarks` — requires JWT

Lists the caller's own saved records — never another user's, with no route parameter that could
ask for one.

```json
{
  "id": "78cfad5b-...", "diagnosisId": "b47d", "gender": "Male", "age": 70,
  "bp": "181", "painType": "Typical Angina", "treatment": "Lifestyle Changes",
  "createdAt": "2026-09-02T19:09:00.594"
}
```

| Status | When |
|---|---|
| `200` | array, empty if none saved yet |
| `401` | missing/invalid token (rejected at the Gateway) |

### `DELETE /api/bookmarks/{id}` — requires JWT

`{id}` is the bookmark's own id, not the diagnosis record's. A correct id belonging to a
different user returns the same `404` as a nonexistent one — no way to probe whether an id
belongs to someone else.

| Status | When |
|---|---|
| `200` | removed, empty body |
| `401` | missing/invalid token |
| `404` | no bookmark with that id for this user |

---

## Security tests — bypass the Gateway on purpose

Point these directly at a service's own port, not `9090`, to prove identity can't be forged even
without the Gateway involved:

| Request | Expected |
|---|---|
| `GET http://localhost:8080/profile` with header `X-User-Id: fake-uuid` (no signature) | `401 {"message":"Invalid identity signature"}` |
| `GET http://localhost:8082/bookmarks` with header `X-User-Id: fake-uuid` (no signature) | `401 {"message":"Invalid identity signature"}` |
| Any request through the Gateway with a valid token **plus** a hand-set `X-User-Id` header | Gateway strips the manual header and uses its own — response reflects the token's real owner, never the spoofed value |

## Cross-user isolation

There is no route on `/api/profile` or `/api/bookmarks` that accepts another user's ID —
identity is always derived from the caller's own token. Confirmed live:

- User A's token on `GET /api/bookmarks` never returns User B's bookmarks.
- User A's token on `DELETE /api/bookmarks/{userB'sBookmarkId}` returns `404`, not `403` — so a
  caller can't distinguish "doesn't exist" from "belongs to someone else."
