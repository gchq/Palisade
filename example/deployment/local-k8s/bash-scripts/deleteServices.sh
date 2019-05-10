#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. $DIR/../../bash-scripts/setScriptPath.sh
"${K8SBASHSCRIPTS}/k8sDeleteConfigureExample.sh" "$K8SBASHSCRIPTS" || true
"${K8SBASHSCRIPTS}/k8sDeleteDataService.sh" "$K8SBASHSCRIPTS"
"${K8SBASHSCRIPTS}/k8sDeletePalisadeService.sh" "$K8SBASHSCRIPTS"
"${K8SBASHSCRIPTS}/k8sDeleteUserService.sh" "$K8SBASHSCRIPTS"
"${K8SBASHSCRIPTS}/k8sDeleteResourceService.sh" "$K8SBASHSCRIPTS"
"${K8SBASHSCRIPTS}/k8sDeletePolicyService.sh" "$K8SBASHSCRIPTS"
"${K8SBASHSCRIPTS}/k8sDeleteConfigureServices.sh" "$K8SBASHSCRIPTS"
"${K8SBASHSCRIPTS}/k8sDeleteConfigService.sh" "$K8SBASHSCRIPTS"
"${K8SBASHSCRIPTS}/k8sDeleteEtcd.sh" "$K8SBASHSCRIPTS"
"${K8SBASHSCRIPTS}/k8sDeletePersistentVolume.sh" "$K8SBASHSCRIPTS"
"${K8SBASHSCRIPTS}/k8sDeleteNginxIngressController.sh" "$K8SBASHSCRIPTS"
kubectl delete -f "${K8SBASHSCRIPTS}/ingress/k8sIngress.yaml"

