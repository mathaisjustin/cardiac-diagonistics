# API Contract

Both endpoints are reached through the API Gateway, not called directly. Per
[ADR-0009](../../00-infrastructure/adr/0009-route-level-authorization-at-gateway.md), both are
**public** routes — no token required to hit them (you can't have a token before you log in).
Error shape and status codes follow [`../../03-cross-cutting/api-conventions.md`](../../03-cross-cutting/api-conventions.md).

## `POST /auth/register`

Creates a new user.

**Request**

```json
{
  "email": "jane@example.com",
  "password": "Sw0rdfish!",
  "firstName": "Jane",
  "lastName": "Doe",
  "phone": "555-0100"
}
```

All five fields required. Validation rules:

| Field | Rule |
|---|---|
| `email` | Non-empty, standard email format, max 255 characters, not already registered. |
| `password` | 8–72 characters, at least one letter and one number — see [`security.md`](./security.md) for the exact regex and why 72 is a hard max, not arbitrary. |
| `firstName` | Non-empty, max 50 characters. No format check beyond that. |
| `lastName` | Non-empty, max 50 characters. No format check beyond that. |
| `phone` | Non-empty. **No format validation** — deliberately not built, see [`../../BACKLOG.md`](../../BACKLOG.md) if this changes later. |

Any failing field is reported in the `400 VALIDATION_ERROR` response's `fields` map (see
[api-conventions](../../03-cross-cutting/api-conventions.md)) — e.g. a request with a 4-character
password and an empty `lastName` returns both under `fields.password` and `fields.lastName` in
the same response, not just the first one found.

**Success — `201 Created`**

```json
{
  "userId": "6f1a2b3c-...",
  "email": "jane@example.com"
}
```

No token — registration and login are separate steps (US-01: registration sends the user to the
login page, doesn't log them in).

**Errors**

| Status | Code | When |
|---|---|---|
| `400` | `VALIDATION_ERROR` | A required field is missing/empty, or the password fails the policy (`fields` names which one — see [`security.md`](./security.md)). |
| `409` | `EMAIL_ALREADY_REGISTERED` | The email is already in use (US-01 acceptance criteria). |
| `503` | `REGISTRATION_UNAVAILABLE` | Kafka didn't acknowledge the registration event — the credential is rolled back (not left half-created), and this tells the client to retry. See [`messaging.md`](./messaging.md) and [ADR-0011](../../00-infrastructure/adr/0011-registration-waits-for-kafka-producer-ack.md). |

## `POST /auth/login`

Authenticates an existing user and issues a token.

**Request**

```json
{
  "email": "jane@example.com",
  "password": "Sw0rdfish!"
}
```

**Success — `200 OK`**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresInMinutes": 60
}
```

See [`security.md`](./security.md) for what's inside the token and why.

**Errors**

| Status | Code | When |
|---|---|---|
| `401` | `INVALID_CREDENTIALS` | Wrong password **or** unknown email — same code and message either way (US-02: never reveal which field was wrong, or a distinct error would let someone enumerate registered emails). |

No `400`/`VALIDATION_ERROR` here — an empty email or password is just a credential that will
never match, so it falls straight into `INVALID_CREDENTIALS` rather than a separate validation
path. There's nothing else to validate about the *shape* of a login request.

## No `/auth/logout` endpoint

Logout is purely client-side — the frontend deletes the token from local storage. There is
nothing for the server to do, so there's no endpoint. See [`security.md`](./security.md) for why.
