#!/bin/bash
# cleanup-minikube.sh - Script to clean up all Helm releases and Kubernetes resources
# Usage: ./cleanup-minikube.sh

set -e

echo "🧹 Starting Minikube cleanup process..."

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

# Check if minikube is running
check_minikube_status() {
  echo "🔍 Checking Minikube status..."
  if ! minikube status | grep -q "Running"; then
    echo "❌ Minikube is not running. Please start it with 'minikube start'."
    exit 1
  fi
}

# Delete all Helm releases in all namespaces
delete_helm_releases() {
  echo "🗑️  Deleting all Helm releases in all namespaces..."

  # Get all namespaces
  namespaces=$(kubectl get namespaces -o jsonpath='{.items[*].metadata.name}')

  for ns in $namespaces; do
    echo "Checking for Helm releases in namespace: $ns"
    releases=$(helm list -n "$ns" -q)

    if [ -n "$releases" ]; then
      echo "Found releases in $ns: $releases"
      for release in $releases; do
        echo "Deleting Helm release: $release in namespace $ns"
        helm uninstall "$release" -n "$ns"
      done
    else
      echo "No Helm releases found in namespace $ns"
    fi
  done

  echo "✅ All Helm releases have been deleted"
}

# Delete all custom resources from Prometheus Operator CRDs
delete_prometheus_crs() {
  echo "🗑️  Deleting Prometheus Operator custom resources..."

  # List of Prometheus Operator CRD kinds to clean up
  crd_kinds=(
    "Alertmanager"
    "PodMonitor"
    "Prometheus"
    "PrometheusRule"
    "ServiceMonitor"
    "ThanosRuler"
  )

  for kind in "${crd_kinds[@]}"; do
    echo "Deleting $kind resources in all namespaces..."
    # Check if the CRD exists before attempting to delete resources
    if kubectl get crd | grep -q "${kind,,}.monitoring.coreos.com"; then
      # Get all resources of this kind in all namespaces
      resources=$(kubectl get "$kind" --all-namespaces -o jsonpath='{range .items[*]}{.metadata.namespace}{" "}{.metadata.name}{"\n"}{end}' 2>/dev/null || echo "")

      if [ -n "$resources" ]; then
        while IFS= read -r line; do
          if [ -n "$line" ]; then
            read -r namespace name <<< "$line"
            echo "Deleting $kind $name in namespace $namespace"
            kubectl delete "$kind" "$name" -n "$namespace" --timeout=60s
          fi
        done <<< "$resources"
      else
        echo "No $kind resources found"
      fi
    else
      echo "CRD for $kind not found, skipping"
    fi
  done
}

# Delete all Prometheus Operator CRDs
delete_prometheus_crds() {
  echo "🗑️  Deleting Prometheus Operator CRDs..."

  # Get all CRDs that belong to the Prometheus Operator
  prometheus_crds=$(kubectl get crd -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' | grep monitoring.coreos.com || echo "")

  if [ -n "$prometheus_crds" ]; then
    for crd in $prometheus_crds; do
      echo "Deleting CRD: $crd"
      kubectl delete crd "$crd" --timeout=60s
    done
  else
    echo "No Prometheus Operator CRDs found"
  fi
}

# Delete all namespaces created for our infrastructure
delete_namespaces() {
  echo "🗑️  Deleting infrastructure namespaces..."

  # List of namespaces to delete
  infra_namespaces=("infrastructure" "monitoring")

  for ns in "${infra_namespaces[@]}"; do
    if kubectl get namespace "$ns" &>/dev/null; then
      echo "Deleting namespace: $ns"
      kubectl delete namespace "$ns" --timeout=120s
    else
      echo "Namespace $ns not found, skipping"
    fi
  done
}

# Kill any port-forwarding processes
kill_port_forwards() {
  echo "🔌 Killing any port-forwarding processes..."

  pf_pids=$(ps aux | grep "kubectl port-forward" | grep -v grep | awk '{print $2}')
  if [ -n "$pf_pids" ]; then
    echo "Killing port-forward processes: $pf_pids"
    for pid in $pf_pids; do
      kill "$pid" 2>/dev/null || true
    done
  else
    echo "No port-forwarding processes found"
  fi
}

# Optionally reset minikube completely
reset_minikube() {
  echo "❓ Do you want to completely reset Minikube? This will delete the entire cluster. (y/n)"
  read -r response
  if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    echo "🔄 Resetting Minikube cluster..."
    minikube delete
    echo "✅ Minikube cluster deleted. You can start a fresh cluster with: minikube start"
  else
    echo "Skipping Minikube reset."
  fi
}

# Main execution
echo "⚠️  WARNING: This script will delete all Helm releases and Kubernetes resources."
echo "      Data in persistent volumes may be lost."
echo "      Are you sure you want to continue? (y/n)"
read -r confirmation

if [[ "$confirmation" =~ ^([yY][eE][sS]|[yY])$ ]]; then
  check_dependencies
  check_minikube_status
  kill_port_forwards
  delete_helm_releases
  delete_prometheus_crs
  delete_prometheus_crds
  delete_namespaces
  reset_minikube

  echo "✅ Cleanup completed!"
else
  echo "Cleanup cancelled. No changes were made."
fi