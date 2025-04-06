#!/bin/bash
# setup-infra.sh - Script to deploy infrastructure components using Helm
# Usage: ./setup-infra.sh

set -e

HELM_VALUES_DIR="."

echo "🚀 Setting up infrastructure components using Helm..."

# Check for required tools
check_dependencies() {
  local missing_deps=0

  # Check for kubectl
  if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl not found. Please install it first."
    missing_deps=1
  fi

  # Check for Helm
  if ! command -v helm &> /dev/null; then
    echo "❌ Helm not found. Please install it first."
    missing_deps=1
  fi

  # Check for Minikube
  if ! command -v minikube &> /dev/null; then
    echo "❌ Minikube not found. Please install it first."
    missing_deps=1
  fi

  if [ $missing_deps -ne 0 ]; then
    echo "Please install missing dependencies and try again."
    exit 1
  fi
}

# Add and update Helm repositories
setup_helm_repos() {
  echo "🔧 Adding Helm repositories..."
  helm repo add bitnami https://charts.bitnami.com/bitnami
  helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
  helm repo add grafana https://grafana.github.io/helm-charts
  helm repo update
}

# Install Prometheus Operator CRDs
install_prometheus_crds() {
  echo "📦 Installing Prometheus Operator CRDs..."

  # Create monitoring namespace if it doesn't exist
  kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -

  # Download and apply CRDs using the kube-prometheus-stack approach
  TMPDIR=$(mktemp -d)
  echo "Downloading CRDs to $TMPDIR"

  # Clone the kube-prometheus repo to get the CRDs
  git clone --depth 1 https://github.com/prometheus-operator/kube-prometheus.git "$TMPDIR/kube-prometheus"

  # Apply only the CRD manifests
  kubectl apply -f "$TMPDIR/kube-prometheus/manifests/setup"

  # Clean up
  rm -rf "$TMPDIR"

  echo "✅ Prometheus CRDs installed successfully"

  # Wait a moment for the CRDs to be established
  echo "⏳ Waiting for CRDs to be established..."
  sleep 10
}

# Deploy PostgreSQL
deploy_postgres() {
  echo "🐘 Deploying PostgreSQL..."
  helm upgrade --install postgres bitnami/postgresql \
    --values "${HELM_VALUES_DIR}/postgres-values.yaml" \
    --namespace infrastructure --create-namespace
}

# Deploy Kafka
deploy_kafka() {
  echo "📨 Deploying Kafka..."
  helm upgrade --install kafka bitnami/kafka \
    --values "${HELM_VALUES_DIR}/kafka-values.yaml" \
    --namespace infrastructure --create-namespace
}

# Deploy Prometheus
deploy_prometheus() {
  echo "📊 Deploying Prometheus..."
  helm upgrade --install prometheus prometheus-community/prometheus \
    --values "${HELM_VALUES_DIR}/prometheus-values.yaml" \
    --namespace monitoring --create-namespace
}

# Deploy Loki
deploy_loki() {
  echo "📃 Deploying Loki (log aggregation)..."
  helm repo add grafana https://grafana.github.io/helm-charts
  helm upgrade --install loki grafana/loki-stack \
    --values "${HELM_VALUES_DIR}/loki-values.yaml" \
    --namespace monitoring --create-namespace
}

# Deploy Tempo
deploy_tempo() {
  echo "🔍 Deploying Tempo (distributed tracing)..."
  helm upgrade --install tempo grafana/tempo \
    --values "${HELM_VALUES_DIR}/tempo-values.yaml" \
    --namespace monitoring --create-namespace
}

# Deploy Grafana
deploy_grafana() {
  echo "📈 Deploying Grafana..."
  helm upgrade --install grafana grafana/grafana \
    --values "${HELM_VALUES_DIR}/grafana-values.yaml" \
    --namespace monitoring --create-namespace

  # Get Grafana admin password
  local grafana_password=$(kubectl get secret -n monitoring grafana -o jsonpath="{.data.admin-password}" | base64 --decode)
  echo "Grafana admin password: $grafana_password"
}

# Wait for deployments to be ready
wait_for_deployments() {
  echo "⏳ Waiting for deployments to become ready..."
  echo "This may take a few minutes..."

  kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=postgresql -n infrastructure --timeout=300s || echo "Still waiting for PostgreSQL pods..."
  kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=kafka -n infrastructure --timeout=300s || echo "Still waiting for Kafka pods..."
  kubectl wait --for=condition=Ready pod -l app=prometheus -n monitoring --timeout=300s || echo "Still waiting for Prometheus pods..."
  kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=grafana -n monitoring --timeout=300s || echo "Still waiting for Grafana pods..."
  kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=loki -n monitoring --timeout=300s || echo "Still waiting for Loki pods..."
  kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=tempo -n monitoring --timeout=300s || echo "Still waiting for Tempo pods..."
}

# Create port forwards for easy access
setup_port_forwards() {
  echo "🔌 Setting up port forwarding for observability stack..."
  kubectl port-forward -n monitoring svc/prometheus-server 9090:80 &
  kubectl port-forward -n monitoring svc/grafana 3000:80 &
  kubectl port-forward -n monitoring svc/loki 3100:3100 &
  kubectl port-forward -n monitoring svc/tempo 3200:3100 &

  echo "✅ Port forwards established:"
  echo "- Prometheus (metrics): http://localhost:9090"
  echo "- Loki (logs): http://localhost:3100"
  echo "- Tempo (traces): http://localhost:3200"
  echo "- Grafana (visualization): http://localhost:3000"
}

# Display connection information
show_connection_info() {
  echo
  echo "📝 Infrastructure Connection Information:"
  echo
  echo "PostgreSQL connection info:"
  echo "--------------------------"
  echo "Host: postgres-postgresql.infrastructure.svc.cluster.local"
  echo "Port: 5432"
  echo "Username: postgres"
  echo "Password: $(kubectl get secret -n infrastructure postgres-postgresql -o jsonpath="{.data.postgres-password}" | base64 --decode)"
  echo "Database: postgres"
  echo
  echo "Kafka connection info:"
  echo "--------------------"
  echo "Bootstrap Server: kafka.infrastructure.svc.cluster.local:9092"
  echo
  echo "Observability Stack:"
  echo "-------------------"
  echo "Prometheus URL (metrics): http://prometheus-server.monitoring.svc.cluster.local"
  echo "Loki URL (logs): http://loki.monitoring.svc.cluster.local:3100"
  echo "Tempo URL (traces): http://tempo.monitoring.svc.cluster.local:3100"
  echo "Grafana URL (visualization): http://grafana.monitoring.svc.cluster.local"
  echo "Grafana admin username: admin"
}

# Main execution
check_dependencies
setup_helm_repos
deploy_postgres
deploy_kafka
deploy_prometheus
deploy_loki
deploy_tempo
deploy_grafana
wait_for_deployments
show_connection_info
echo
echo "Would you like to set up port forwarding for easy access to Prometheus and Grafana? (y/n)"
read -r response
if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
  setup_port_forwards
fi

echo "✅ Infrastructure setup complete!"