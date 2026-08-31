# Backend: Maven for builds, Spring Cloud Gateway for the API Gateway

All five Spring Boot services (UserProfile, Authentication, Diagnosis, Bookmark, API Gateway)
build with Maven — simpler and more common in the Spring Boot ecosystem than Gradle, so it's
easier for three devs to stay consistent on without a build-tooling learning curve. The API
Gateway itself is implemented with Spring Cloud Gateway, not the older Netflix Zuul (1.x, in
maintenance mode within Spring Cloud) — Spring Cloud Gateway is the current standard and
integrates directly with Eureka for service lookup.
