# Use OpenJDK 21 as base image
FROM openjdk:21-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Create .env file for Docker environment
RUN echo "DB_USERNAME=chat_user" > .env && \
    echo "DB_PASSWORD=chat_password" >> .env && \
    echo "JWT_KEY=mySecretKey" >> .env

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the application with Docker profile
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "target/chat-0.0.1-SNAPSHOT.jar"]