#!/bin/bash

# Function to display usage information
show_usage() {
  echo "Usage: $0 [OPTION]"
  echo "Options:"
  echo "  start         Start all containers"
  echo "  stop          Stop all containers"
  echo "  restart       Restart all containers"
  echo "  logs          Show logs from the market data service"
  echo "  db-logs       Show logs from the database"
  echo "  ps            Show container status"
  echo "  build         Rebuild the market data service container"
  echo "  db-shell      Access the PostgreSQL shell"
  echo "  help          Show this help message"
}

# Function to check if .env file exists
check_env_file() {
  if [ ! -f .env ]; then
    echo "Warning: .env file not found. Creating template from .env.template"
    if [ -f .env.template ]; then
      cp .env.template .env
      echo "Please edit .env file and set your Upstox API credentials"
    else
      echo "Error: .env.template not found. Please create a .env file with your Upstox API credentials"
      exit 1
    fi
  fi
}

# Check for docker-compose command
if command -v docker-compose &> /dev/null; then
  DOCKER_COMPOSE_CMD="docker-compose"
elif command -v docker compose &> /dev/null; then
  DOCKER_COMPOSE_CMD="docker compose"
else
  echo "Error: docker-compose or 'docker compose' not found. Please install Docker and Docker Compose."
  exit 1
fi

# Process command line arguments
case "$1" in
  start)
    check_env_file
    echo "Starting containers..."
    $DOCKER_COMPOSE_CMD up -d
    ;;
  stop)
    echo "Stopping containers..."
    $DOCKER_COMPOSE_CMD down
    ;;
  restart)
    echo "Restarting containers..."
    $DOCKER_COMPOSE_CMD down
    check_env_file
    $DOCKER_COMPOSE_CMD up -d
    ;;
  logs)
    echo "Showing market data service logs..."
    $DOCKER_COMPOSE_CMD logs -f marketdata-service
    ;;
  db-logs)
    echo "Showing database logs..."
    $DOCKER_COMPOSE_CMD logs -f postgres
    ;;
  ps)
    echo "Container status:"
    $DOCKER_COMPOSE_CMD ps
    ;;
  build)
    check_env_file
    echo "Rebuilding market data service..."
    $DOCKER_COMPOSE_CMD build marketdata-service
    ;;
  db-shell)
    echo "Accessing PostgreSQL shell..."
    $DOCKER_COMPOSE_CMD exec postgres psql -U postgres -d quantz_market_data
    ;;
  *)
    show_usage
    ;;
esac