# Messaging

Authentication is the **publisher** in the one Kafka flow in this system — see
[ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md)
for why this replaced the earlier two-way design, and
[`docs/00-infrastructure/kafka-message-bus/`](../../00-infrastructure/kafka-message-bus/) (once
written) for the full topic reference across all services.

## What it publishes

**When**: immediately after a registration is successfully stored (after the credential is
already committed — this is fire-and-forget from Auth's side, it doesn't wait for or care whether
UserProfile has consumed it).

**Payload**: the new user's ID + whatever basic profile fields were collected at registration
(exact field set TBD when UserProfile Service is grilled — e.g. name).

**Who consumes it**: UserProfile Service only. Authentication does not consume anything back —
no round trip (that's the whole point of ADR-0010).

## What happens if UserProfile is down when this publishes

The event sits in Kafka until UserProfile is back up to consume it — registration still succeeds
from the user's point of view (they can log in immediately, since Auth already stored their
credential synchronously). The only consequence is their profile record is created a bit later
than usual. This is the "smaller race" ADR-0010 calls out — full handling of it belongs in
UserProfile Service's own docs, not here.
