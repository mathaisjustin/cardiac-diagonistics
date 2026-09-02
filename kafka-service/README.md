# kafka-service

Dockerized Kafka broker (KRaft mode, no Zookeeper) shared by the other cardiac-diagnostics services as a messaging backbone.

## Usage

```bash
cd kafka-service
docker compose up -d
```

This starts a single-broker Kafka container on the `cardiac-net` Docker network.

- From the host: `localhost:9092`
- From another container on `cardiac-net`: `kafka:19092`

No topics are pre-created — auto topic creation is enabled, and each service is responsible for the topics it needs.

## Connecting other services

Other services can join the same network to reach the broker by container name instead of `localhost`:

```yaml
networks:
  default:
    external:
      name: cardiac-net
```

Then use `kafka:19092` as the bootstrap server.
