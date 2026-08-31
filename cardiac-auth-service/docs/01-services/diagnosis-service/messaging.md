# Messaging

**This service doesn't use Kafka at all.**

This was a genuine question when this service was being planned — Bookmark Service needs to
confirm a diagnosis record exists before saving a reference to it, and the instinct was "we
already have Kafka set up, reuse it." But every interaction Diagnosis Service has needs an
**immediate answer**:

- A client browsing or searching is sitting there waiting for results.
- Bookmark Service, when a user clicks "bookmark," is waiting to know *right now* whether the
  record is valid before it can tell the user "bookmarked!" or show an error.

Kafka is for the opposite case — fire-and-forget, no one waiting on a response (the one real
example in this system is Authentication's registration hand-off to UserProfile, see
[ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md)).
Forcing Bookmark's validation check through Kafka would mean building a request/reply pattern on
top of it — two topics, both services as producer *and* consumer, a correlation ID to match
replies to requests, and a timeout policy for when no reply comes. That's exactly the two-way
publisher/consumer complexity ADR-0010 removed elsewhere in this system, reintroduced here for a
case that's fundamentally a synchronous question with a synchronous answer.

So Bookmark Service calls `GET /diagnosis/{id}` **directly** instead — see
[`flows.md`](./flows.md) and [`api-contract.md`](./api-contract.md).
