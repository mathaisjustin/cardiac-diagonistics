# API Contract

Both routes are **protected** — per the Gateway's
[route protection map](../../00-infrastructure/api-gateway/README.md#route-protection-map), a
valid JWT is required, and the Gateway forwards the caller's identity as `X-User-Id` /
`X-User-Email` headers (see
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)). Neither
route takes a user ID as a path or query parameter — the caller can only ever act on their own
profile, identified from the header, never anyone else's.

Only two routes exist. No creation route (profiles are created only via the Kafka consumer — see
[`messaging.md`](./messaging.md)), no deletion route.

## `GET /profile`

Returns the caller's own profile.

**Request**: nothing — identity comes from `X-User-Id`.

**Behavior**: looks up the profile by `user_id` (from the header) and returns it, combined with
the `email` from the `X-User-Email` header — `email` is never read from this service's own
database, since it doesn't store one (see [`data-model.md`](./data-model.md)).

**Response**: `email` (from the header), `firstName`, `lastName`, `phone` (from the database).

**Edge case**: if the profile doesn't exist yet — the Kafka event hasn't been consumed yet,
per the small race noted in Authentication's
[ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md)
— see [`flows.md`](./flows.md) for how this is handled.

## `PUT /profile`

Updates the caller's own profile.

**Request**: `firstName`, `lastName`, `phone` (whichever fields the user is changing — `email` is
not accepted here at all, since UserProfile never owns it).

**Behavior**:
- Validates the fields are present/well-formed before saving (US-09 acceptance criteria).
- Updates the row identified by `user_id` (from the header).
- Returns a confirmation (US-09: "a confirmation message shows once changes are saved").

**Response**: the updated profile.
