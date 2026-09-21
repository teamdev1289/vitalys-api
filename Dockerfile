# ==========================================
# Multi-stage Dockerfile for Vitalys API
# Java: 21 (Eclipse Temurin)
# Framework: Spring Boot 3.5.x
# ==========================================

# ------------------------------------------
# Stage 1: Build
# ------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy dependency configuration first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build package
COPY src ./src
RUN mvn clean package -DskipTests

# ------------------------------------------
# Stage 2: Production Runtime
# ------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create non-root user for security
RUN addgroup -S vitalys && adduser -S vitalys -G vitalys

# Copy the built jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to non-root user
RUN chown vitalys:vitalys app.jar

USER vitalys

# Expose default application port
EXPOSE 8080

# Configure JVM flags optimized for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
