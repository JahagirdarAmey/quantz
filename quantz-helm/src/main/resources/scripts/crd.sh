#!/bin/bash
# install-prometheus-crds.sh - Script to install Prometheus Operator CRDs
# Usage: ./install-prometheus-crds.sh

set -e

echo "📦 Installing Prometheus Operator CRDs..."

# Create monitoring namespace if it doesn't exist
kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -

# Method 1: Install CRDs using helm
echo "Method 1: Installing CRDs using Helm (recommended)"
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install only the CRDs by setting a special flag
helm install prometheus-crds prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --set prometheusOperator.createCustomResource=true \
  --set prometheusOperator.admissionWebhooks.enabled=false \
  --set prometheusOperator.admissionWebhooks.patch.enabled=false \
  --set prometheusOperator.kubeletService.enabled=false \
  --set prometheus.enabled=false \
  --set alertmanager.enabled=false \
  --set grafana.enabled=false \
  --set nodeExporter.enabled=false \
  --set kube-state-metrics.enabled=false

# Alternative Method if Helm approach fails
if [ $? -ne 0 ]; then
  echo "Helm method failed, trying alternative method..."
  # Clean up failed helm release
  helm uninstall prometheus-crds -n monitoring || true

  # Method 2: Apply CRDs using kubectl
  echo "Method 2: Installing CRDs using kubectl directly"

  # Create a temporary directory
  TMPDIR=$(mktemp -d)
  echo "Downloading CRDs to $TMPDIR"

  # Download the bundle of CRDs
  curl -sL https://github.com/prometheus-operator/prometheus-operator/releases/download/v0.65.1/bundle.yaml > "$TMPDIR/bundle.yaml"

  # Extract and apply only the CRDs
  grep -Pzo 'apiVersion: apiextensions\.k8s\.io/v1\nkind: CustomResourceDefinition\n.*?---' "$TMPDIR/bundle.yaml" | sed 's/---$//' > "$TMPDIR/crds.yaml"

  # Apply CRDs
  kubectl apply -f "$TMPDIR/crds.yaml"

  # Clean up
  rm -rf "$TMPDIR"
fi

echo "✅ Prometheus CRDs installed successfully"
echo "⏳ Waiting for CRDs to be established..."
sleep 10

# Verify CRDs were installed
echo "Verifying CRD installation:"
kubectl get crds | grep monitoring.coreos.com