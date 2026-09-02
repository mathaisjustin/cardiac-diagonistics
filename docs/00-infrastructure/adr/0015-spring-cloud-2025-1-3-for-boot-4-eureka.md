# Spring Cloud 2025.1.3 for Eureka on Spring Boot 4

Every backend service in this system runs Spring Boot 4.0.8. Spring Cloud (the umbrella project
that ships the Eureka server/client starters) publishes release trains version-locked to a
specific Boot line, and Boot 4 is new enough that most trains don't target it yet.

The first train tried, `2025.0.0`, still targets Boot 3.5 — both `eureka-service` (server) and a
client service with the Eureka client starter added crashed on startup with
`NoClassDefFoundError: org/springframework/boot/web/context/WebServerInitializedEvent`, a class
Boot 4 renamed. Maven's dependency resolution doesn't catch this kind of break — it only checks
declared version compatibility, not actual binary compatibility, so the failure only surfaces at
runtime.

Rather than pin `eureka-service` to an older Spring Boot 3.x version in isolation (which was tried
briefly and worked, but split the codebase across two Boot majors for no reason beyond
Eureka), or hand-roll a manual REST client against Eureka's plain HTTP API (also tried, and
functionally correct, but not the standard implementation and missing things the real client
gives for free — client-side load-balancing integration, registry caching/refresh, retry/backoff
tuning), we tested a newer train: **Spring Cloud `2025.1.3`**, confirmed compatible with Boot
4.0.8 — clean startup, both server and client. Every service (including `eureka-service` itself)
is pinned to this version, so the whole system runs one consistent Boot + Spring Cloud pairing,
using the standard `spring-cloud-starter-netflix-eureka-server` / `-client` starters with no
custom registration code.
