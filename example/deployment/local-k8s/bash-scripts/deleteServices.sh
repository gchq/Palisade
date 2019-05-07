#!/usr/bin/env bash
set -e
DIR=$(dirname "$0")
. "$DIR/../../bash-scripts/setScriptPath.sh"
"$K8SBASHSCRIPTS/k8sDeleteExample.sh" "$K8SBASHSCRIPTS" || true
"$K8SBASHSCRIPTS/k8sDeleteDataService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeletePalisadeService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeleteUserService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeleteResourceService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeletePolicyService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeleteConfigureServices.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeleteConfigService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeleteEtcd.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeletePersistentVolume.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeleteNginxIngressController.sh" "$K8SBASHSCRIPTS"
kubectl delete -f "$K8SBASHSCRIPTS/ingress/k8sIngress.yaml"

