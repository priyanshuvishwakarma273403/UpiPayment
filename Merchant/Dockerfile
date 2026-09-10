FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=target/*.jar
COPY  app.jar
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app.jar"]
