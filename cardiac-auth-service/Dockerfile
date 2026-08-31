FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/cardiac-auth-service.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]