# API Contract

Base path: `/profile`, port `8080`. Both routes require headers `X-User-Id` and `X-User-Email`
(see the trust-model note in [`README.md`](./README.md) — there is no token validation, these
headers are taken at face value). Neither route takes a user ID as a path/query parameter — the
caller only ever acts on the identity in their own headers.

## `GET /profile`

**Request**: headers only, no body.

**Success — `200 OK`**

```json
{
  "email": "jane@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "contact": "555-0100",
  "department": "Cardiology"
}
```

`email` is echoed straight from `X-User-Email` (not read from the DB); the rest comes from the
`profiles` table.

**Errors**

| Status | When |
|---|---|
| `404` | No profile row for this `userId` yet — the Kafka event from registration hasn't been consumed. `{message}` explains. See [`flows.md`](./flows.md). |
| `400` | `X-User-Id` or `X-User-Email` header missing. |
| `500` | Database error, or any other unhandled exception. |

## `PUT /profile`

Full update — always send all four editable fields.

**Request**

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "contact": "555-0101",
  "department": "Cardiology"
}
```

All four fields `@NotBlank`. `email` is never accepted here.

**Success — `200 OK`** — same shape as `GET /profile`'s response.

**Errors**

| Status | When |
|---|---|
| `400` | A field is blank (`{message:"Validation failed", validationErrors:{field: msg}}`), or the request body is malformed JSON (`{message:"Invalid request body"}`). |
| `404` | No profile row exists yet for this `userId` — `PUT` does not create one. |
| `400` | Missing header. |
| `500` | Database or unhandled error. |

## Error response shape

`{ timestamp, status, error, message }` for all handlers, plus a `validationErrors` map on
bean-validation failures.
