FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY --from=build /build/target/cardiac-diagnosis-service.jar app.jar

EXPOSE 8083

USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]
