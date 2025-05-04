FROM maven:3.8.6-openjdk-17-slim AS build

WORKDIR /build

# Copy the pom.xml file
COPY pom.xml .

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create the runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /build/target/quantz-api-market-data-service-*.jar app.jar

# Create volume mount points
VOLUME /app/logs

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "/app/app.jar"]