FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/reservations-service-0.0.1-SNAPSHOT.jar /app/
EXPOSE 8083
CMD ["java", "-jar", "reservations-service-0.0.1-SNAPSHOT.jar"]