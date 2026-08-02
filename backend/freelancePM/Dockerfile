# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the application, skipping tests to speed up the build on Render
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar
# Expose the port (Render assigns dynamically via $PORT, defaulting to 8081 if not set)
EXPOSE 8081
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
