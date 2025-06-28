#!/bin/bash

# Quantz Market Data Service - Observability Stack Deployment Script
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="quantz-market-data"
DOCKER_COMPOSE_FILE="docker-compose.yml"
ENV_FILE=".env"
APP_SERVICE="quantz-api-market-data-service"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    # Check if Dockerfile exists
    if [ ! -f "quantz-api-service/quantz-api-market-data-service/src/main/resources/Dockerfile" ]; then
        log_error "Dockerfile not found at quantz-api-service/quantz-api-market-data-service/src/main/resources/Dockerfile"
        log_error "Please ensure you're in the project root directory and the Dockerfile exists."
        exit 1
    fi

    # Check if pom.xml exists
    if [ ! -f "quantz-api-service/quantz-api-market-data-service/pom.xml" ]; then
        log_error "pom.xml not found at quantz-api-service/quantz-api-market-data-service/pom.xml"
        log_error "Please ensure you're in the project root directory."
        exit 1
    fi

    # Check available ports
    local ports=(3000 3100 3200 8080 9090 5433 4318 4319 4320 4321)
    for port in "${ports[@]}"; do
        if netstat -tuln 2>/dev/null | grep -q ":$port " || ss -tuln 2>/dev/null | grep -q ":$port "; then
            log_warning "Port $port is already in use. Please free it or modify the configuration."
        fi
    done

    log_success "Prerequisites check completed"
}

# Create directory structure
create_directories() {
    log_info "Creating directory structure..."

    mkdir -p grafana/provisioning/datasources
    mkdir -p grafana/provisioning/dashboards
    mkdir -p logs
    mkdir -p prometheus/rules
    mkdir -p loki/rules
    mkdir -p init-scripts

    log_success "Directory structure created"
}

# Create environment file
create_env_file() {
    if [ -f "$ENV_FILE" ]; then
        log_info "Environment file already exists. Skipping creation."
        return
    fi

    log_info "Creating environment file..."

    cat > $ENV_FILE << 'EOF'
# Quantz Market Data Service Configuration
COMPOSE_PROJECT_NAME=quantz-market-data

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
LOG_LEVEL_QUANTZ=INFO
LOG_LEVEL_MICROMETER=WARN
LOG_LEVEL_ROOT=INFO

# Database Configuration
POSTGRES_DB=quantz_market_data
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres_password_here
DATABASE_URL=jdbc:postgresql://postgres:5432/quantz_market_data

# Tracing Configuration
TRACING_SAMPLING_PROBABILITY=0.1
DETAILED_LOGGING=false

# Grafana Configuration
GF_SECURITY_ADMIN_PASSWORD=admin123
GF_USERS_ALLOW_SIGN_UP=false

# Upstox Configuration (REPLACE WITH YOUR ACTUAL VALUES)
UPSTOX_CLIENT_ID=your-upstox-client-id-here
UPSTOX_CLIENT_SECRET=your-upstox-client-secret-here
UPSTOX_REDIRECT_URI=http://localhost:8080/api/upstox/auth/callback

# Data Scraper Configuration
DATA_SCRAPER_ENABLED=true
DATA_SCRAPER_CRON=0 0 16 * * MON-FRI

# JVM Configuration
JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC
EOF

    log_success "Environment file created: $ENV_FILE"
    log_warning "Please update UPSTOX_CLIENT_ID and UPSTOX_CLIENT_SECRET in $ENV_FILE"
}

# Initial setup
initial_setup() {
    log_info "Running initial setup..."
    check_prerequisites
    create_directories
    create_env_file
    log_success "Initial setup completed"
}

# Build application only
build_app() {
    log_info "Building Spring Boot application..."
    docker-compose build $APP_SERVICE
    log_success "Application built successfully"
}

# Start infrastructure services first
start_infrastructure() {
    log_info "Starting infrastructure services..."
    docker-compose up -d postgres prometheus loki tempo grafana

    log_info "Waiting for infrastructure services to be ready..."
    sleep 20

    # Wait for PostgreSQL
    log_info "Waiting for PostgreSQL to be ready..."
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
            log_success "PostgreSQL is ready"
            break
        fi
        log_info "Waiting for PostgreSQL... (attempt $attempt/$max_attempts)"
        sleep 2
        ((attempt++))
    done

    log_success "Infrastructure services are ready"
}

# Start application service
start_app() {
    log_info "Starting Quantz Market Data Service..."
    docker-compose up -d $APP_SERVICE

    # Wait for application to be ready
    log_info "Waiting for application to be ready..."
    local max_attempts=60
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
            log_success "Application is ready and healthy"
            break
        fi
        log_info "Waiting for application... (attempt $attempt/$max_attempts)"
        sleep 5
        ((attempt++))
    done

    if [ $attempt -gt $max_attempts ]; then
        log_error "Application failed to start within expected time"
        docker-compose logs $APP_SERVICE
        return 1
    fi
}

# Full startup process
start_services() {
    initial_setup
    start_infrastructure
    build_app
    start_app

    # Start remaining services
    log_info "Starting remaining services..."
    docker-compose up -d

    log_success "All services started successfully"
    show_urls
}

# Quick restart for development
dev_restart() {
    log_info "Development restart - rebuilding and restarting application..."

    # Stop application
    docker-compose stop $APP_SERVICE

    # Rebuild application
    build_app

    # Start application
    start_app

    log_success "Application restarted successfully"
}

# Hot reload for Java changes
hot_reload() {
    log_info "Hot reloading application with Java changes..."

    # Build new image
    log_info "Building updated application..."
    docker-compose build --no-cache $APP_SERVICE

    # Stop old container
    log_info "Stopping current application..."
    docker-compose stop $APP_SERVICE
    docker-compose rm -f $APP_SERVICE

    # Start new container
    log_info "Starting updated application..."
    docker-compose up -d $APP_SERVICE

    # Wait for startup
    log_info "Waiting for application to start..."
    sleep 10

    # Check health
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
            log_success "Application reloaded successfully with new changes"
            break
        fi
        log_info "Waiting for reloaded application... (attempt $attempt/$max_attempts)"
        sleep 3
        ((attempt++))
    done

    if [ $attempt -gt $max_attempts ]; then
        log_error "Application failed to start after reload"
        show_logs
        return 1
    fi
}

# Check service health
check_health() {
    log_info "Checking service health..."

    local services=(
        "Application:http://localhost:8080/actuator/health"
        "Prometheus:http://localhost:9090/-/healthy"
        "Grafana:http://localhost:3000/api/health"
        "Loki:http://localhost:3100/ready"
        "Tempo:http://localhost:3200/ready"
    )

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    for service in "${services[@]}"; do
        local name=$(echo $service | cut -d':' -f1)
        local endpoint=$(echo $service | cut -d':' -f2-)

        if curl -sf "$endpoint" > /dev/null 2>&1; then
            echo -e "✅ $name: ${GREEN}HEALTHY${NC}"
        else
            echo -e "❌ $name: ${RED}UNHEALTHY${NC}"
        fi
    done
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

# Display service URLs
show_urls() {
    echo ""
    log_info "🚀 Service URLs:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🔥 Quantz Market Data API: http://localhost:8080"
    echo "📊 Grafana Dashboard:      http://localhost:3000 (admin/admin123)"
    echo "📈 Prometheus:             http://localhost:9090"
    echo "📋 Loki:                   http://localhost:3100"
    echo "🔍 Tempo:                  http://localhost:3200"
    echo "🗄️  PostgreSQL:            localhost:5433 (host port)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "📚 API Endpoints:"
    echo "   Health Check:           http://localhost:8080/actuator/health"
    echo "   Metrics:                http://localhost:8080/actuator/prometheus"
    echo "   Info:                   http://localhost:8080/actuator/info"
    echo "   All Endpoints:          http://localhost:8080/actuator"
    echo ""
}

# Generate test data
generate_test_data() {
    log_info "Generating test data..."

    # Wait for application to be ready
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
            break
        fi
        log_info "Waiting for application to be ready... (attempt $attempt/$max_attempts)"
        sleep 5
        ((attempt++))
    done

    if [ $attempt -gt $max_attempts ]; then
        log_warning "Application is not ready, skipping test data generation"
        return
    fi

    # Generate some test requests
    log_info "Making test API calls..."
    for i in {1..20}; do
        curl -s http://localhost:8080/actuator/health > /dev/null || true
        curl -s http://localhost:8080/actuator/info > /dev/null || true
        curl -s http://localhost:8080/actuator/metrics > /dev/null || true
        sleep 0.5
    done

    log_success "Test data generated - check Grafana dashboards!"
}

# Stop services
stop_services() {
    log_info "Stopping services..."
    docker-compose down
    log_success "Services stopped"
}

# Cleanup
cleanup() {
    log_info "Cleaning up..."
    docker-compose down -v --remove-orphans
    docker system prune -f
    log_success "Cleanup completed"
}

# Show logs
show_logs() {
    local service=${1:-$APP_SERVICE}
    log_info "Showing logs for $service..."
    docker-compose logs -f $service
}

# Restart specific service
restart_service() {
    local service=${1:-$APP_SERVICE}
    log_info "Restarting $service..."
    docker-compose restart $service
    log_success "$service restarted"
}

# Show service status
show_status() {
    log_info "Service Status:"
    docker-compose ps
}

# Development workflow
dev_workflow() {
    echo ""
    log_info "🔄 Development Workflow Commands:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "For Java code changes:"
    echo "  $0 reload           # Rebuild and restart app with changes"
    echo "  $0 dev-restart      # Quick restart for development"
    echo ""
    echo "For monitoring:"
    echo "  $0 logs [service]   # View logs (default: app)"
    echo "  $0 health          # Check all service health"
    echo "  $0 status          # Show container status"
    echo ""
    echo "For testing:"
    echo "  $0 test            # Generate test traffic"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

# Show help
show_help() {
    echo "Quantz Market Data Service - Observability Stack"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "🚀 Main Commands:"
    echo "  start              Start all services (full setup)"
    echo "  stop               Stop all services"
    echo "  restart            Restart all services"
    echo "  status             Show service status"
    echo "  health             Check service health"
    echo "  urls               Show service URLs"
    echo ""
    echo "🔧 Development Commands:"
    echo "  reload             Rebuild and restart app (for Java changes)"
    echo "  dev-restart        Quick restart for development"
    echo "  build              Build application only"
    echo "  logs [service]     Show logs (default: app)"
    echo "  test               Generate test traffic"
    echo ""
    echo "🧹 Maintenance Commands:"
    echo "  cleanup            Stop services and clean up volumes"
    echo "  restart-app        Restart only the application"
    echo "  setup              Run initial setup only"
    echo ""
    echo "ℹ️  Information Commands:"
    echo "  workflow           Show development workflow tips"
    echo "  help               Show this help"
    echo ""
    echo "Examples:"
    echo "  $0 start           # First time setup and start"
    echo "  $0 reload          # After making Java changes"
    echo "  $0 logs            # View application logs"
    echo "  $0 health          # Check if everything is working"
    echo ""
}

# Main script logic
case "${1:-start}" in
    "start")
        start_services
        ;;
    "stop")
        stop_services
        ;;
    "restart")
        stop_services
        start_services
        ;;
    "restart-app")
        restart_service $APP_SERVICE
        ;;
    "dev-restart")
        dev_restart
        ;;
    "reload")
        hot_reload
        ;;
    "build")
        build_app
        ;;
    "setup")
        initial_setup
        ;;
    "status")
        show_status
        ;;
    "health")
        check_health
        ;;
    "urls")
        show_urls
        ;;
    "logs")
        show_logs $2
        ;;
    "test")
        generate_test_data
        ;;
    "cleanup")
        cleanup
        ;;
    "workflow")
        dev_workflow
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    *)
        log_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac