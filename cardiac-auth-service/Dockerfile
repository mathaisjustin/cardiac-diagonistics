FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

# Includes the full JDK + Maven (not just a JRE) plus the source/pom, so you
# can `docker exec` into the running container and run `mvn test`/`verify`/
# `sonar:sonar` directly - no Java or Maven install needed on the host.
COPY --from=build /build/pom.xml ./pom.xml
COPY --from=build /build/src ./src
COPY --from=build /build/target/cardiac-auth-service.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
