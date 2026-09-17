# ==========================================
# STAGE 1 - BUILD
# ==========================================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom first for better Docker layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build Spring Boot application
RUN mvn clean package -DskipTests


# ==========================================
# STAGE 2 - RUN
# ==========================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy generated JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Payment Service port
EXPOSE 8084

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]