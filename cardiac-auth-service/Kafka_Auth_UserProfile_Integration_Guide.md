# Kafka Integration Guide — Auth Service → UserProfile Service

## Purpose

This document explains the Kafka contract between the Authentication Service and UserProfile Service. It covers both local development and Docker-based execution.

## 1. Architecture

```text
Auth Service
     |
     | user.registered
     v
   Kafka
     |
     v
UserProfile Service
```

## 2. Kafka Infrastructure

| Setting | Value |
|---|---|
| Kafka image | `apache/kafka:3.7.0` |
| Mode | KRaft (no Zookeeper) |
| Container | `cardiac-kafka-service` |
| Docker network | `cardiac-net` |
| Host connection | `localhost:9092` |
| Docker connection | `kafka:19092` |
| Topic | `user.registered` |
| Topic creation | Auto-creation enabled |

## 3. Event Contract

Topic:

```text
user.registered
```

JSON:

```json
{
  "userId": 4,
  "email": "Aravindtest@gmail.com",
  "firstName": "Kafka01",
  "lastName": "Test01",
  "contactNumber": "1234567890",
  "department": "Cardiology"
}
```

Fields:

- `userId`
- `email`
- `firstName`
- `lastName`
- `contactNumber`
- `department`

**Never send `password` or `passwordHash`.**

The Kafka message key is `userId` converted to a String.

## 4. What UserProfile Must Implement

1. Add Spring Kafka.
2. Configure the correct bootstrap server.
3. Create a matching `UserRegisteredEvent`.
4. Add a Kafka listener for `user.registered`.
5. Use a stable group ID such as `user-profile-service`.
6. Create/save the profile from the event.
7. Make processing idempotent using `userId`.

### Example DTO

```java
public record UserRegisteredEvent(
        Long userId,
        String email,
        String firstName,
        String lastName,
        String contactNumber,
        String department
) {
}
```

### Example consumer

```java
@KafkaListener(
        topics = "user.registered",
        groupId = "user-profile-service"
)
public void consume(UserRegisteredEvent event) {
    // Check whether profile already exists.
    // If it does not exist, create and save it.
}
```

## 5. Local Development

If Kafka runs in Docker but the Spring Boot services run directly from IntelliJ:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

Start Kafka:

```powershell
docker compose up -d
```

Then run Auth Service and UserProfile Service locally.

### Local flow

```text
Auth Service (host)
      |
      | localhost:9092
      v
Kafka (Docker)
      |
      v
UserProfile Service (host)
```

## 6. Docker Development

When the Spring Boot services also run inside Docker, attach them to `cardiac-net` and use:

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:19092
```

Do **not** use `localhost:9092` from one container to reach Kafka.

### Docker flow

```text
Auth container
      |
      | kafka:19092
      v
Kafka container
      |
      | kafka:19092
      v
UserProfile container
```

## 7. Manual Verification

Kafka is already producing messages to `user.registered`.

To inspect messages from PowerShell:

```powershell
docker exec -it cardiac-kafka-service /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:19092 --topic user.registered --from-beginning
```

This is only a **manual test consumer**. The actual UserProfile application must consume using `@KafkaListener`.

## 8. End-to-End Flow

```text
POST /register
      |
      v
Auth Service
      |
      +--> Save credentials
      |
      +--> Publish UserRegisteredEvent
                    |
                    v
              user.registered
                    |
                    v
             Kafka broker
                    |
                    v
          UserProfile consumer
                    |
                    v
              profile DB
```

## 9. Important Rules

- Exact topic: `user.registered`
- Exact fields: `userId`, `email`, `firstName`, `lastName`, `contactNumber`, `department`
- No password/passwordHash in Kafka
- Local application → `localhost:9092`
- Docker application → `kafka:19092`
- Stable consumer group → `user-profile-service`
- Consumer should be idempotent
- Prefer a unique `userId` constraint in the profile database

## 10. Troubleshooting

### `localhost:9092` connection refused

Check:

```powershell
docker ps
```

and confirm `cardiac-kafka-service` is running.

### `kafka:19092` connection fails in Docker

Check that the application container is attached to:

```text
cardiac-net
```

### No messages received

Verify:

- Topic is exactly `user.registered`
- Listener is running
- Bootstrap server is correct
- Consumer group is configured correctly

### Duplicate profiles

Use `userId` as the unique identity and make the consumer idempotent.
