FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/kafka-tracking-api.jar kafka-tracking-api.jar

ENTRYPOINT ["java", "-jar", "kafka-tracking-api.jar"]
