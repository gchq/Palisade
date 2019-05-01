#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"
"$K8SBASHSCRIPTS/k8sDeployEtcd.sh"
# Create a pod shared volume
kubectl apply -f ./persistent-volume/k8sPersistentVolume.yaml
kubectl apply -f ./persistent-volume/k8sPersistentVolumeClaim.yaml
"$K8SBASHSCRIPTS/k8sDeployConfigService.sh"
"$K8SBASHSCRIPTS/k8sDeployConfigureServices.sh"
"$K8SBASHSCRIPTS/k8sDeployPolicyService.sh"
"$K8SBASHSCRIPTS/k8sDeployResourceService.sh"
"$K8SBASHSCRIPTS/k8sDeployUserService.sh"
"$K8SBASHSCRIPTS/k8sDeployPalisadeService.sh"
"$K8SBASHSCRIPTS/k8sDeployDataService.sh"
# wait for the data-service pod to be running
sleep 5
DATA_POD=$(kubectl get pod -l app=data-service -o jsonpath="{.items[0].metadata.name}")
RESOURCE_POD=$(kubectl get pod -l app=resource-service -o jsonpath="{.items[0].metadata.name}")
kubectl cp "$K8SBASHSCRIPTS/../../../resources/exampleEmployee_file0.avro" "$DATA_POD:/data"
kubectl exec $RESOURCE_POD -- bash -c '[ -f "/data/exampleEmployee_file0.avro" ] && echo "file exists (OK)" || echo "ERROR file NOT found"'
kubectl exec $DATA_POD     -- bash -c '[ -f "/data/exampleEmployee_file0.avro" ] && echo "file exists (OK)" || echo "ERROR file NOT found"'




