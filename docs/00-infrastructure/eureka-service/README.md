# Eureka Service (Service Discovery)

A **standalone Spring Boot 4 application** (`eureka-service`, Java 17, Maven, Netflix Eureka
Server via `spring-cloud-starter-netflix-eureka-server`), its own container, port `8761`. Does
not register with itself (`register-with-eureka: false`, `fetch-registry: false`) — it's the
registry, not a client of one.

## What it does

A directory of "who's alive and where." Every other service in this system
(`cardiac-auth-service`, `cardiac-user-profile-service`, `cardiac-diagnosis-service`,
`cardiac-bookmark-service`) registers itself on startup via `spring-cloud-starter-netflix-eureka-client`
and sends a heartbeat every 30 seconds; a service that stops sending heartbeats is evicted after
90 seconds. There is no API Gateway yet, so nothing currently *looks up* addresses through
Eureka — services register, but nothing consumes the registry programmatically until the Gateway
is built.

## Dashboard

`http://localhost:8761` — the built-in Eureka web UI, lists every registered instance and its
status. Raw JSON: `GET http://localhost:8761/eureka/apps`.

## Version note

Spring Boot 4 is new enough that Spring Cloud's release trains lagged behind it — the first
train tried (`2025.0.0`) was still built against Boot 3.5 and crashed both the server and client
starters with a `NoClassDefFoundError` at startup (a class Boot 4 renamed). **Spring Cloud
`2025.1.3`** is the first train confirmed compatible with Boot 4.0.8 — that's what every service
in this system (including this one) is pinned to. See
[ADR-0015](../adr/0015-spring-cloud-2025-1-3-for-boot-4-eureka.md).

## Config & Environment

| Variable | Default | Purpose |
|---|---|---|
| `EUREKA_HOSTNAME` | `localhost` | Hostname this registry advertises itself under. |

Every client service reads:

| Variable | Default | Purpose |
|---|---|---|
| `EUREKA_URL` | `http://localhost:8761/eureka` | This registry's `defaultZone`. |
| `EUREKA_INSTANCE_HOSTNAME` | `localhost` | Hostname that service registers under (set to its container name in `docker-compose.yml`, e.g. `cardiac-diagnosis-service`). |

Each client also sets `eureka.instance.prefer-ip-address: true`, so registered instances are
reachable by container IP rather than relying on Docker's internal DNS resolving the hostname.
