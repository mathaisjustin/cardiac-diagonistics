# Registration waits for Kafka's producer acknowledgment before responding

Following [ADR-0010](./0010-registration-owned-by-auth-single-direction-kafka.md), Authentication
publishes a new user's profile data (first name, last name, phone, user ID) to Kafka for
UserProfile Service to consume — and that data lives *nowhere else*. Auth's own database only
stores `email`, `password_hash`, and the user ID; the profile fields exist only in the Kafka
message.

Kafka's 7-day topic retention protects against UserProfile being temporarily down when the
message is already in the topic — but it does nothing if the message never made it into the
topic in the first place. A pure fire-and-forget publish (send and don't check) would mean a
transient Kafka outage silently discards that person's name and phone number forever, while their
credential still exists and login still works — a a permanent, undetectable data loss with no
error shown to anyone.

Instead, Authentication waits for the Kafka **producer's acknowledgment** (confirmation the
broker has durably stored the message) before responding "registered successfully" to the
client. This does not mean waiting for UserProfile to consume it — only that the broker confirms
it received it. If the broker can't be reached, Authentication **rolls back the credential it
just created** and returns an error — otherwise the email would be permanently stuck as "already
registered" with no profile ever created and no way to retry. Registration fails visibly and
cleanly, instead of the profile data disappearing with no trace or a dead-end account being left
behind.
