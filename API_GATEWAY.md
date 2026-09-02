# Cardiac Diagnostics System --- API Gateway Handover

## 1. Purpose

This document records the API Gateway work completed so far, the current
known issue, and the implementation plan for the next developer.

The intended Gateway is the single external entry point for the Cardiac
Diagnostics microservices.

## 2. Current Technology

-   Java 17
-   Spring Boot 4.0.8
-   Spring Cloud 2025.1.3
-   Spring Cloud Gateway Server Web MVC
-   Maven
-   JJWT 0.12.6
-   Gateway port: `9090`

The project uses **Gateway Server Web MVC**, not WebFlux. Do not copy
WebFlux `GlobalFilter`/`ServerWebExchange` examples into this project.

## 3. Service Ports and Routes

  Service          Backend port Gateway path          JWT
  -------------- -------------- --------------------- -----------------------
  Auth                     8081 `/api/auth/**`        Public auth endpoints
  User Profile             8080 `/api/profile/**`     Required
  Bookmark                 8082 `/api/bookmarks/**`   Required
  Diagnosis                8083 `/api/diagnosis/**`   Public
  API Gateway              9090 ---                   ---

Backend controller paths confirmed from the services:

-   Auth: `/api/auth`
-   Profile: `/profile`
-   Diagnosis: `/diagnosis`
-   Bookmark: `/bookmarks`

For Profile, Diagnosis and Bookmark, the external `/api` prefix must be
removed before forwarding.

Example:

``` text
GET /api/diagnosis
        ↓
GET /diagnosis
        ↓
http://localhost:8083/diagnosis
```

## 4. Work Completed

### 4.1 Gateway project

Created `cardiac-api-gateway` with:

``` text
Spring Boot 4.0.8
Java 17
Spring Cloud 2025.1.3
spring-cloud-starter-gateway-server-webmvc
```

Gateway port is:

``` yaml
server:
  port: 9090
```

The Gateway successfully started on port 9090.

### 4.2 JJWT

JJWT dependencies were initially placed incorrectly under
`dependencyManagement`. They were moved to `dependencies`.

Required dependencies:

``` xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

The project successfully compiled after this correction.

### 4.3 JWT service

`JwtService` was created in:

``` text
security/JwtService.java
```

It validates the JWT signature and extracts:

``` text
subject -> userId
email   -> email
expiration
```

The Gateway uses the same `JWT_SECRET` as Auth.

Configuration:

``` yaml
jwt:
  secret: ${JWT_SECRET}
```

For local PowerShell execution:

``` powershell
$env:JWT_SECRET="THE_SAME_SECRET_USED_BY_AUTH"
```

Do not commit the real secret.

### 4.4 Java route configuration

Gateway routes were moved to a Java `RouterFunction` configuration:

``` text
GatewayRoutesConfig.java
```

The routes are intended to be:

``` text
/api/auth/**      -> http://localhost:8081
/api/profile/**   -> http://localhost:8080
/api/diagnosis/** -> http://localhost:8083
/api/bookmarks/** -> http://localhost:8082
```

The Java route approach is being used so protected routes can apply
route-specific MVC filters.

## 5. Security Design

The intended security model is:

``` text
Client
  |
  | Authorization: Bearer <JWT>
  v
API Gateway
  |
  | validate JWT
  | extract userId/email
  v
trusted internal headers
  |
  v
Backend
```

Never trust client-provided:

``` text
X-User-Id
X-User-Email
```

For Profile:

``` text
JWT -> X-User-Id
JWT -> X-User-Email
```

For Bookmark:

``` text
JWT -> X-User-Id
```

Bookmark does **not** need `X-User-Email`.

Diagnosis currently needs neither identity header nor JWT.

## 6. Current Route Security

The intended rules are:

``` text
Auth       -> public
Diagnosis  -> public
Profile    -> JWT required
Bookmark   -> JWT required
```

Do not reintroduce a global Servlet JWT filter that protects every
route, because that would also protect the public Diagnosis endpoints.

## 7. Controller Requirements Confirmed

### Profile

The Profile controller expects:

``` text
X-User-Id
X-User-Email
```

### Bookmark

The Bookmark controller expects:

``` text
X-User-Id
```

Endpoints:

``` text
GET    /bookmarks
DELETE /bookmarks/{id}
```

### Diagnosis

The Diagnosis controller exposes:

``` text
GET  /diagnosis
POST /diagnosis
GET  /diagnosis/search
GET  /diagnosis/analysis/treatment
GET  /diagnosis/validate/{id}
GET  /diagnosis/{id}
```

No user header is required by the shown controller.

## 8. Testing Already Done

### Gateway startup

Completed successfully:

``` text
localhost:9090
```

### Auth

Auth routing through the Gateway was previously tested successfully.

### Profile

Profile routing through the Gateway was previously tested.

The route uses `/api/profile/**` externally and forwards to
`/profile/**`.

### Diagnosis

The test:

``` http
GET http://localhost:9090/api/diagnosis
```

returned:

``` text
500 Internal Server Error
```

Gateway log:

``` text
ResourceAccessException:
I/O error on GET request for "http://localhost:8083/diagnosis"
```

Root cause shown:

``` text
java.nio.channels.ClosedChannelException
```

This points to a Gateway-to-Diagnosis connectivity problem, not JWT
protection.

## 9. Immediate Next Task

First test Diagnosis directly:

``` http
GET http://localhost:8083/diagnosis
```

No JWT is required.

If it fails, check:

``` powershell
docker ps
```

If Diagnosis is Dockerized, verify that the host exposes port 8083, for
example:

``` text
0.0.0.0:8083->8083/tcp
```

The application setting:

``` yaml
server:
  port: 8083
```

only means the application listens on 8083 inside its runtime/container.
A Docker host mapping is also required when the Gateway runs on the
host.

Do not modify JWT logic until direct Diagnosis connectivity works.

## 10. Remaining Work

### A. Finish connectivity verification

Verify:

``` text
Gateway -> Auth
Gateway -> Profile
Gateway -> Diagnosis
Gateway -> Bookmark
```

### B. Finish JWT protected routes

Verify:

``` text
Bookmark without JWT -> 401
Profile without JWT  -> 401
Bookmark invalid JWT  -> 401
Profile invalid JWT   -> 401
Bookmark valid JWT    -> forwarded
Profile valid JWT     -> forwarded
```

### C. Verify trusted identity headers

For Profile:

``` text
X-User-Id
X-User-Email
```

must come from the validated JWT.

For Bookmark:

``` text
X-User-Id
```

must come from the validated JWT.

### D. Test header impersonation

Send a valid JWT but also send forged:

``` text
X-User-Id: another-user
X-User-Email: another@example.com
```

The Gateway must remove/overwrite those values and forward only values
derived from the JWT.

### E. Clean up unused experimental classes

Verify that old experimental classes are not still present or
referenced:

``` text
GatewayFilterConfiguration.java
GatewayUserFilters.java
JwtAuthenticationFilter.java
```

The intended final structure is:

``` text
src/main/java/com/elsevier/cardiac_api_gateway
│
├── CardiacApiGatewayApplication.java
├── GatewayRoutesConfig.java
│
└── security
    └── JwtService.java
```

Only remove classes after confirming they are no longer referenced.

## 11. Testing Sequence

Use this order:

### 1. Auth

``` http
POST http://localhost:9090/api/auth/login
```

Obtain JWT.

### 2. Diagnosis

``` http
GET http://localhost:9090/api/diagnosis
```

No JWT.

Expected:

``` text
200 OK
```

### 3. Bookmark without JWT

``` http
GET http://localhost:9090/api/bookmarks
```

Expected:

``` text
401 Unauthorized
```

### 4. Bookmark with JWT

``` http
GET http://localhost:9090/api/bookmarks
Authorization: Bearer <JWT>
```

Expected Gateway flow:

``` text
JWT validation
   ↓
extract userId
   ↓
X-User-Id
   ↓
Bookmark :8082
```

### 5. Profile without JWT

``` http
GET http://localhost:9090/api/profile
```

Expected:

``` text
401 Unauthorized
```

### 6. Profile with JWT

``` http
GET http://localhost:9090/api/profile
Authorization: Bearer <JWT>
```

Expected:

``` text
JWT validation
   ↓
extract userId/email
   ↓
X-User-Id + X-User-Email
   ↓
Profile :8080
```

### 7. Forged headers

Send a valid JWT plus forged identity headers.

Expected result:

``` text
JWT identity wins
Client headers are not trusted
```

## 12. Eureka --- Future Phase

Another developer is working on Eureka.

Current Gateway routing uses direct URLs:

``` text
http://localhost:8081
http://localhost:8080
http://localhost:8083
http://localhost:8082
```

After Eureka is stable, replace hardcoded service locations with service
discovery.

Target:

``` text
Gateway
   |
   v
Eureka
   |
   +-- AUTH-SERVICE
   +-- USER-PROFILE-SERVICE
   +-- DIAGNOSIS-SERVICE
   +-- BOOKMARK-SERVICE
```

Do this after direct routing and JWT behavior are stable.

## 13. Production Hardening --- Later

The current local setup exposes backend ports directly.

For production, prefer:

``` text
Internet
   |
   v
API Gateway
   |
   +-- Auth
   +-- Profile
   +-- Diagnosis
   +-- Bookmark
```

Later improvements:

-   Keep backend services private from the public network.
-   Store JWT secrets in proper secret management.
-   Add rate limiting.
-   Add CORS configuration if a browser frontend is used.
-   Add centralized Gateway error handling.
-   Add correlation/request IDs.
-   Add metrics and monitoring.
-   Add role/authority claims if authorization requires them.
-   Add automated integration tests.

## 14. Handover Checklist

The next developer should start here:

``` text
[ ] Check Diagnosis direct request on localhost:8083
[ ] Fix Docker/host port mapping if required
[ ] Retest Diagnosis through Gateway :9090
[ ] Test Bookmark without JWT -> 401
[ ] Login through Gateway and obtain JWT
[ ] Test Bookmark with JWT
[ ] Test Profile without JWT -> 401
[ ] Test Profile with JWT
[ ] Verify X-User-Id comes from JWT
[ ] Verify X-User-Email comes from JWT for Profile
[ ] Verify Bookmark receives only X-User-Id
[ ] Test forged identity headers
[ ] Remove unused experimental classes
[ ] Run mvn clean compile
[ ] Run full route tests
[ ] Commit/push Gateway changes
[ ] Integrate Eureka later
```

## 15. Final Target

``` text
                         CLIENT
                            |
                            | HTTP + JWT
                            v
                    +----------------+
                    |  API GATEWAY   |
                    |     :9090      |
                    +-------+--------+
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
          AUTH           PROFILE        DIAGNOSIS
          :8081           :8080           :8083
                            |
                            v
                         BOOKMARK
                           :8082

Protected:
  Profile  -> JWT -> userId + email
  Bookmark -> JWT -> userId

Public:
  Auth
  Diagnosis

Later:
  Gateway -> Eureka -> Services
```

## 16. Handover Summary

The Gateway project, port configuration, Spring Cloud Gateway MVC setup,
JJWT dependency setup, JWT service, and initial routes have been
implemented/compiled. Auth and Profile routing were tested earlier.

The main unresolved issue at handover is the Diagnosis connectivity
failure:

``` text
Gateway :9090
    -> localhost:8083/diagnosis
    -> ClosedChannelException
```

The next developer should solve that connectivity issue first, then
complete protected-route JWT testing and trusted-header verification,
and finally proceed with Eureka integration.
