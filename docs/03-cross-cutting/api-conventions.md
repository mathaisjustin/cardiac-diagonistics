# API Conventions

Shared rules every service's API follows, so each service's `api-contract.md` only needs to
document what's specific to it, not redefine this from scratch.

## Success responses

Return the resource directly — no wrapper envelope. E.g. `POST /auth/register` returns the
created user, `GET /bookmarks` returns an array of bookmarks.

## Error responses

Every error, from every service, follows the same shape:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Human-readable summary, safe to show a user or log",
    "fields": {
      "password": "Must be at least 8 characters with a letter and a number"
    }
  }
}
```

- `code` — a stable, SCREAMING_SNAKE_CASE identifier the **frontend switches on**. Never changes
  wording; safe to key logic off of.
- `message` — human-readable, for logs or a generic fallback display.
- `fields` — **only present when the error is a per-field validation failure.** Maps a field
  name to what's wrong with it, so the frontend can highlight the specific input instead of
  showing one generic banner. Omitted for errors that aren't about a specific field (e.g. "record
  not found").

## HTTP status codes

| Status | Meaning | Example |
|---|---|---|
| `200` | Success (read or update) | `GET /bookmarks`, `PUT /profile` |
| `201` | Success (created something) | `POST /auth/register`, `POST /bookmarks` |
| `400` | Validation failed — the request itself is malformed/invalid | Missing required field, password too weak |
| `401` | Not authenticated, or credentials wrong | No/invalid/expired JWT, wrong login |
| `404` | The specific resource doesn't exist | `GET /diagnosis/{id}` for an unknown ID |
| `409` | Conflict with existing state | Registering an already-used email |
| `503` | A dependency this request needed is unavailable | Kafka unreachable during registration |
| `500` | Unexpected server error | Anything not covered above |

`403 Forbidden` isn't used anywhere in this system yet — there's no role/permission distinction
(every Registered User has the same access), so the only access-control failure is "not
authenticated at all," which is `401`.

## Where this is enforced

The **Gateway** handles the `401` case for missing/invalid tokens on protected routes, before a
request ever reaches a service (see
[ADR-0005](../00-infrastructure/adr/0005-stateless-jwt-validation-at-gateway.md) and
[ADR-0009](../00-infrastructure/adr/0009-route-level-authorization-at-gateway.md)) — so a
service's own `api-contract.md` only needs to document the errors *it* can produce once a
request has already passed the Gateway.
