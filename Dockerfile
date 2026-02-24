# Le JAR est buildé en amont par la CI (mvn clean verify)
# et copié dans target/ avant le docker build.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/masterannonce.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
