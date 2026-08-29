# Messaging

Authentication is the **publisher** in the one Kafka flow in this system — see
[ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md)
for why this replaced the earlier two-way design, and
[`docs/00-infrastructure/kafka-message-bus/`](../../00-infrastructure/kafka-message-bus/) (once
written) for the full topic reference across all services.

## What it publishes

**When**: immediately after a registration is successfully stored (the credential is already
committed at this point).

**Payload**: the new user's ID, first name, last name, and phone number — see
[`api-contract.md`](./api-contract.md) for the full field list collected at registration.

**Who consumes it**: UserProfile Service only. Authentication does not consume anything back —
no round trip (that's the whole point of ADR-0010).

**Email is deliberately not in here.** UserProfile never stores a copy of it — every request it
receives already carries the caller's email as a Gateway-forwarded `X-User-Email` header (per
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)), read
live from the validated JWT on each call. Storing a second copy would just be a value that could
drift from Authentication's — see
[ADR-0013](../../00-infrastructure/adr/0013-email-never-duplicated-into-userprofile.md).

## Reliability: this is not fire-and-forget

Auth's own database never stores the profile fields (first name, last name, phone) — see
[`data-model.md`](./data-model.md). They exist **only** in this Kafka message. So Authentication
**waits for the Kafka producer's acknowledgment** (confirmation the broker durably stored the
message) before responding "registered successfully" to the client — see
[ADR-0011](../../00-infrastructure/adr/0011-registration-waits-for-kafka-producer-ack.md) for why.

This is *not* the same as waiting for UserProfile to consume it — Auth doesn't know or care when
that happens. It only confirms the message safely entered the topic. Once it has, Kafka's 7-day
retention takes over from there.

## What happens if UserProfile is down when this publishes

Because the message is already safely in the topic (producer ack received), it just sits there —
retained for 7 days — until UserProfile is back up to consume it. Registration still succeeds
from the user's point of view (they can log in immediately, since Auth already stored their
credential synchronously). The only consequence is their profile record is created a bit later
than usual. This is the "smaller race" ADR-0010 calls out — full handling of it belongs in
UserProfile Service's own docs, not here.

## What happens if the Kafka broker itself is unreachable

The producer ack never comes back. Registration **fails** — but the credential was already
committed in step 4 of [`flows.md`](./flows.md), so Auth **rolls that back** (deletes the just-
created user record) before returning the error to the client. Without this, the email would be
permanently stuck as "already taken" with no profile ever created and no way to retry — a
dead-end account nobody could fix by trying again. This is the tradeoff ADR-0011 makes
deliberately: a visible, retryable, *clean* failure instead of silently losing someone's profile
data forever, and without leaving a broken half-registered account behind either.
