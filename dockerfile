FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar /app/
EXPOSE 8083
CMD ["java", "-jar", "*.jar"]