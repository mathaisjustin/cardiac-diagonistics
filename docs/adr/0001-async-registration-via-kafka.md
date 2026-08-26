# Registration hands off from UserProfile to Authentication via Kafka, not a direct call

UserProfile Service owns registration input (name, email, password) but Authentication Service
owns credential storage and login. We could have UserProfile call Authentication synchronously
to store the credential, but we're keeping the case study's design: UserProfile publishes the
new user's credentials to Kafka, and Authentication consumes them asynchronously. This decouples
the two services (UserProfile doesn't need Authentication to be up to accept a registration) at
the cost of eventual consistency — a user could theoretically try to log in before Authentication
has processed the event. How that race is handled is still open — see the "Immediate next
questions" note in this ADR's PR/discussion, to be captured in its own ADR once decided.
