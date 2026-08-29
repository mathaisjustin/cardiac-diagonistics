# Messaging

**This service doesn't use Kafka**, for the same reason as Diagnosis Service (see its own
[`messaging.md`](../diagnosis-service/messaging.md)): every interaction here needs an immediate
answer — a user waiting to know their bookmark saved, waiting for their list to load, waiting for
a removal to confirm. Nothing here is fire-and-forget.

## The one direct call: confirming a record with Diagnosis Service

At `POST /bookmarks` time only (not on every view — see
[ADR-0014](../../00-infrastructure/adr/0014-bookmark-stores-snapshot-not-reference.md)), Bookmark
Service calls Diagnosis Service directly:

```
GET /diagnosis/{id}
```

routed via Eureka lookup, the same pattern as any other service-to-service call — not through the
API Gateway (that's only for client-to-service traffic) and not hardcoded to an address.

**Why not Kafka for this**: covered in depth in Diagnosis Service's
[`messaging.md`](../diagnosis-service/messaging.md) — a request/reply pattern here would
reintroduce the two-way publisher/consumer complexity [ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md)
removed elsewhere, for an interaction that's fundamentally synchronous.

**If Diagnosis Service (or the external API behind it) is down** when this call happens:
bookmarking fails visibly — the user sees an error, nothing is saved. This only affects
*creating* a new bookmark; viewing or removing existing bookmarks never depends on Diagnosis
Service being up at all, since those never call it (see [`api-contract.md`](./api-contract.md)).
