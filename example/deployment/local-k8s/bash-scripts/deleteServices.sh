#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"
"$K8SBASHSCRIPTS/k8sDeleteExample.sh"
"$K8SBASHSCRIPTS/k8sDeleteDataService.sh"
"$K8SBASHSCRIPTS/k8sDeletePalisadeService.sh"
"$K8SBASHSCRIPTS/k8sDeleteUserService.sh"
"$K8SBASHSCRIPTS/k8sDeleteResourceService.sh"
"$K8SBASHSCRIPTS/k8sDeletePolicyService.sh"
"$K8SBASHSCRIPTS/k8sDeleteConfigureServices.sh"
"$K8SBASHSCRIPTS/k8sDeleteConfigService.sh"
"$K8SBASHSCRIPTS/k8sDeleteEtcd.sh"
kubectl delete -f ./persistent-volume/k8sPersistentVolumeClaim.yaml
kubectl delete -f ./persistent-volume/k8sPersistentVolume.yaml


