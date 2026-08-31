# API Contract

Both routes are **protected** — per the Gateway's
[route protection map](../../00-infrastructure/api-gateway/README.md#route-protection-map), a
valid JWT is required, and the Gateway forwards the caller's identity as `X-User-Id` /
`X-User-Email` headers (see
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)). Neither
route takes a user ID as a path or query parameter — the caller can only ever act on their own
profile, identified from the header, never anyone else's. Error shape and status codes follow
[api-conventions](../../03-cross-cutting/api-conventions.md).

Only two routes exist. No creation route (profiles are created only via the Kafka consumer — see
[`messaging.md`](./messaging.md)), no deletion route.

## `GET /profile`

Returns the caller's own profile.

**Request**: nothing — identity comes from `X-User-Id` / `X-User-Email`.

**Success — `200 OK`**

```json
{
  "email": "jane@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "phone": "555-0100"
}
```

`email` comes from the `X-User-Email` header, the rest from the database (see
[`data-model.md`](./data-model.md)).

**Errors**

| Status | Code | When |
|---|---|---|
| `404` | `PROFILE_NOT_READY` | The Kafka event hasn't been consumed yet — see the timing gap noted in Authentication's [ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md). A dedicated code (not a generic "not found") so the frontend can specifically show "setting up your profile…" and retry, instead of a scary error — see [`flows.md`](./flows.md). |

## `PUT /profile`

Replaces the caller's editable fields. **Full update, not partial** — always send all three
fields, even ones that didn't change (simpler than partial-field semantics; the frontend already
has the current values from a prior `GET /profile` to pre-fill the edit form).

**Request**

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "phone": "555-0101"
}
```

`email` is never accepted here — UserProfile doesn't own it (see [`data-model.md`](./data-model.md)).

**Validation rules** — same limits as when these fields were first collected at registration,
for consistency:

| Field | Rule |
|---|---|
| `firstName` | Non-empty, max 50 characters. |
| `lastName` | Non-empty, max 50 characters. |
| `phone` | Non-empty. No format validation, per earlier decision. |

**Success — `200 OK`** — the updated profile, same shape as `GET /profile`'s response, plus a
confirmation the frontend shows (US-09: "a confirmation message shows once changes are saved").

**Errors**

| Status | Code | When |
|---|---|---|
| `400` | `VALIDATION_ERROR` | A field fails its rule above — `fields` names each one that failed. |
| `404` | `PROFILE_NOT_READY` | Same edge case as `GET /profile` — extremely unlikely here in practice, since the frontend only shows an edit form after a successful `GET /profile` already loaded the current values. |
