# API Contract

Base path: `/api/auth`. Called directly on port `8081` today — there's no API Gateway in front of
it yet. Every route below is public (`permitAll`) **except** `change-password`, which requires a
valid access token.

## `POST /api/auth/register`

**Request**

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

All six fields are `@NotBlank` (`email` also `@Email`). There is no password strength rule
enforced beyond non-blank — no length/character-class regex.

**Success — `201 Created`**, empty body.

**Errors**

| Status | When |
|---|---|
| `400` | A field is blank/missing (`{message:"Validation failed", validationErrors:{field: msg}}`), or the request body itself is missing/unparseable (`{message:"Request body is required"}`). |
| `409` | Email already registered — `{message:"Email already registered"}`. |
| `503` | The Kafka publish of `user.registered` failed — `{message:"Registration failed because the messaging service is unavailable"}`. The DB-saved user row is rolled back (the whole method is `@Transactional`), so a failed registration leaves no trace. See [`messaging.md`](./messaging.md). |

## `POST /api/auth/login`

**Request**

```json
{ "email": "jane@example.com", "password": "Sw0rdfish1" }
```

**Success — `200 OK`**

```json
{ "accessToken": "eyJhbGciOi...", "refreshToken": "base64url-random-string" }
```

**Errors**

| Status | When |
|---|---|
| `401` | Wrong password or unknown email — always `{message:"Invalid email or password"}`, same message either way so a caller can't enumerate registered emails. |
| `400` | `email`/`password` blank. |

## `POST /api/auth/refresh`

Rotates a refresh token for a new access + refresh token pair.

**Request**

```json
{ "refreshToken": "base64url-random-string" }
```

**Success — `200 OK`** — same shape as login: `{ accessToken, refreshToken }`. The old refresh
token row is deleted; a new one is issued.

**Errors**

| Status | When |
|---|---|
| `401` | Token not found, revoked, or expired — `{message}` names which ("Invalid refresh token" / "Refresh token has been revoked" / "Refresh token has expired"). Also 401 if the user the token belongs to no longer exists. |

## `POST /api/auth/logout`

**Request**

```json
{ "refreshToken": "base64url-random-string" }
```

Marks the token's row `revoked = true` (does not delete it). **Success — `204 No Content`**.

**Errors**

| Status | When |
|---|---|
| `401` | Token hash not found — `{message:"Invalid refresh token"}`. |

## `POST /api/auth/change-password`

**Requires** `Authorization: Bearer <accessToken>`.

**Request**

```json
{ "oldPassword": "Sw0rdfish1", "newPassword": "N3wPassw0rd" }
```

Not `@Valid` — blank values are not rejected by bean validation (only `InvalidPasswordException`
catches a wrong `oldPassword`).

**Success — `200 OK`** — `{ "message": "Password changed successfully" }`. Also revokes the
user's existing refresh token (they'll need to log in again on other devices/sessions).

**Errors**

| Status | When |
|---|---|
| `401` | No/invalid/expired access token — `{message:"Authentication required"}`. |
| `400` | `oldPassword` doesn't match — `{message:"Old password is incorrect"}`. |
| `404` | User row no longer exists (effectively unreachable in practice). |

## Error response shape

All error bodies are a flat JSON object, `{ "message": "..." }`, except validation failures,
which add a `validationErrors` map keyed by field name. There is no `timestamp`/`path`/`status`
field in the body (status is only in the HTTP status line).
