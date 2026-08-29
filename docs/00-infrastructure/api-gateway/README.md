# API Gateway

A **standalone Spring Boot application** (Spring Cloud Gateway, per
[ADR-0007](../adr/0007-backend-build-and-gateway-tooling.md)) — the single entry point for every
request the frontend makes. No service is reachable directly; everything goes through here.

## What it does

1. **Routes** each request to the right service, by asking Eureka where that service currently
   lives (see [`../eureka-discovery/`](../eureka-discovery/) once written).
2. **Validates JWTs itself**, statelessly, with no call back to Authentication — see
   [ADR-0005](../adr/0005-stateless-jwt-validation-at-gateway.md).
3. **Decides which routes need a token at all** — not every route is protected — see
   [ADR-0009](../adr/0009-route-level-authorization-at-gateway.md).
4. **Forwards identity downstream** as trusted headers once a token is validated, so services
   don't parse JWTs themselves — see
   [ADR-0012](../adr/0012-gateway-forwards-identity-via-headers.md).
5. Applies CORS rules for the frontend's origin.

## The filter chain (per request)

```mermaid
flowchart TD
    Req[Incoming request] --> Route{Is this route<br/>in the protected list?}
    Route -- "no (public)" --> Fwd[Forward to service<br/>no identity headers]
    Route -- "yes (protected)" --> HasToken{Authorization header<br/>present and valid JWT?}
    HasToken -- no --> Reject[401 Unauthorized]
    HasToken -- yes --> Extract[Extract userId + email from token]
    Extract --> AddHeaders["Add X-User-Id, X-User-Email headers"]
    AddHeaders --> FwdAuthed[Forward to service]
```

"Valid" means: signature checks out against the shared key, and it hasn't expired. Nothing more
— the Gateway doesn't call Authentication to check anything (ADR-0005).

## Route protection map

The source of truth for "does this route need a token." Grows as each service is grilled;
routes not yet decided are marked TBD rather than guessed at.

| Route | Service | Protection |
|---|---|---|
| `POST /auth/register` | Authentication | Public |
| `POST /auth/login` | Authentication | Public |
| `GET /profile` | UserProfile | **Protected** |
| `PUT /profile` | UserProfile | **Protected** |
| `GET /diagnosis` | Diagnosis | Public |
| `GET /diagnosis/{id}` | Diagnosis | Public |
| `GET /diagnosis/search` | Diagnosis | **Protected** — restricted to Registered Users, a deliberate deviation from the original "Guest or Registered" backlog spec |
| `GET /diagnosis/analysis` | Diagnosis | **Protected** — US-06 scopes this to Registered Users |
| `POST /bookmarks` | Bookmark | **Protected** |
| `GET /bookmarks` | Bookmark | **Protected** |
| `DELETE /bookmarks/{id}` | Bookmark | **Protected** |

Every route is now decided. All Bookmark routes require an account (US-07: "Guests are prompted
to log in or register if they try to bookmark").

## What downstream services receive

For a protected route, the service behind the Gateway sees:
- `X-User-Id` — the caller's user ID, straight from the validated token's `userId` claim.
- `X-User-Email` — the caller's email, from the token's `email` claim.

The service trusts these outright — it has no way to reach the internet except through the
Gateway, so nothing but the Gateway could have set them. It does **not** re-validate the JWT or
re-check the headers against anything.

For a public route, no identity headers are added — there may not even be a token.

## Config & Environment

| Variable | Purpose |
|---|---|
| `SERVER_PORT` | Port this service listens on. |
| `JWT_SECRET` | Same signing key Authentication uses — needed to verify tokens locally. |
| `EUREKA_URL` | Where to look up service addresses. |
| `CORS_ALLOWED_ORIGIN` | The frontend's origin. |
