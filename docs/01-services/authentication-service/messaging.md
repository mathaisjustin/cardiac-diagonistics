# Messaging

Authentication is a **publisher only** — it never consumes anything.

## What it publishes

**When**: inside `AuthService.register()` (which is `@Transactional`), immediately after the new
`users` row is saved, and **before** the HTTP response is returned. The publish call blocks
(`kafkaTemplate.send(...).get()`) waiting for the producer's send future — so the client's
`201 Created` response only comes back once Kafka has acknowledged the message.

**Topic**: `user.registered`

**Key**: the new user's `userId` (UUID string)

**Payload** (`UserRegisteredEvent`, JSON):

```json
{
  "userId": "6f1a2b3c-...",
  "firstName": "Jane",
  "lastName": "Doe",
  "contactNumber": "555-0100",
  "department": "Cardiology"
}
```

**Email is deliberately not in here.** Every request to UserProfile Service carries the caller's
email via the `X-User-Email` header (see [ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md) —
today, without a Gateway built yet, that header is expected to come from wherever the Gateway
will eventually sit; UserProfile's own docs cover its current trust model).

**Producer config**: `acks=all`, `retries=1`, `delivery.timeout.ms=10000`,
`request.timeout.ms=5000`, `max.block.ms=10000`, key/value serializers are `StringSerializer` /
`JacksonJsonSerializer`.

**Who consumes it**: UserProfile Service only, idempotently by `userId` — see its
[`messaging.md`](../user-profile-service/messaging.md).

## Reliability: not fire-and-forget

Auth's own database never stores first name, last name, contact number, or department — they
exist **only** in this Kafka message. So the publish call blocks for the producer ack before the
HTTP response goes out, and if that ack never comes (`KafkaPublishException`), the whole
`register()` transaction rolls back — the just-created `users` row is undone, and the client gets
a `503`. No half-registered account is left behind; no profile data is silently lost.

If Kafka is reachable but **UserProfile Service itself is down**, the message just sits in the
topic until UserProfile comes back and consumes it — registration still succeeds from the client's
point of view, they can log in immediately, and their profile record is created a bit later than
usual.
