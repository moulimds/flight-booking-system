# Use official OpenJDK 17 lightweight runtime image
FROM eclipse-temurin:17-jre-alpine

# Set working directory inside container
WORKDIR /app

# Copy compiled JAR file into container
COPY target/*.jar app.jar

# Expose Check-in Service port
EXPOSE 8082

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
