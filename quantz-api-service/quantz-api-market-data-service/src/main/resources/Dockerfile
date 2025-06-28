FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy pre-built JAR (you need to build this locally first)
COPY target/quantz-api-market-data-service-*.jar app.jar

# Create volume mount points
VOLUME /app/logs

# Expose application port
EXPOSE 8080

# Set environment variables for observability
ENV SPRING_PROFILES_ACTIVE=prod
ENV MANAGEMENT_OTLP_TRACING_ENDPOINT=http://tempo:4317
ENV MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://tempo:9411/api/v2/spans
ENV MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*
ENV MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for observability and performance
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0"

# Set the entrypoint with JVM options
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]