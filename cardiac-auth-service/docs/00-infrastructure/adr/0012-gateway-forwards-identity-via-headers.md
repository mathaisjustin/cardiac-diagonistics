# Gateway forwards identity via trusted headers, not the raw JWT

Following [ADR-0002](./0002-centralized-jwt-validation-at-gateway.md) and
[ADR-0005](./0005-stateless-jwt-validation-at-gateway.md), the Gateway already validates every
JWT itself. Downstream services still need to know *who* is calling — e.g. UserProfile Service's
`GET /profile` returns the caller's own profile, with no user ID in the URL.

Rather than forwarding the raw token and making every service parse it again (duplicating
token-decoding logic ADR-0002 was written to avoid), the Gateway extracts the identity claims
from the already-validated token and forwards them as plain trusted headers —
`X-User-Id` and `X-User-Email` — to the downstream service. The service just reads the header;
it has no JWT-handling code of its own.

This only works because services are not reachable except through the Gateway (no direct
external access to any service's port) — a service trusts these headers precisely because
nothing but the Gateway can be the one setting them.
