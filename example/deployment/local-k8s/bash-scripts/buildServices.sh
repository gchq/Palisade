#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"
echo "K8SBASHSCRIPTS is set to"
echo $K8SBASHSCRIPTS
"$K8SBASHSCRIPTS/k8sDeployEtcd.sh" "$K8SBASHSCRIPTS"
# Create a pod shared volume
"$K8SBASHSCRIPTS/k8sDeployNginxIngressController.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeployPersistentVolume.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeployConfigService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeployConfigureServices.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeployPolicyService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeployResourceService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeployUserService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeployPalisadeService.sh" "$K8SBASHSCRIPTS"
"$K8SBASHSCRIPTS/k8sDeployDataService.sh" "$K8SBASHSCRIPTS"
kubectl apply -f "$K8SBASHSCRIPTS/ingress/k8sIngress.yaml"
# wait for the data-service pod to be running
sleep 5
export DATA_POD=$(kubectl get pod -l app=data-service -o jsonpath="{.items[0].metadata.name}")
export RESOURCE_POD=$(kubectl get pod -l app=resource-service -o jsonpath="{.items[0].metadata.name}")
kubectl cp "$K8SBASHSCRIPTS/../../../resources/exampleEmployee_file0.avro" "$DATA_POD:/data"
kubectl exec $RESOURCE_POD -- bash -c '[ -f "/data/exampleEmployee_file0.avro" ] && echo "file exists (OK)" || echo "ERROR file NOT found"'
kubectl exec $DATA_POD     -- bash -c '[ -f "/data/exampleEmployee_file0.avro" ] && echo "file exists (OK)" || echo "ERROR file NOT found"'

echo "Check the pods are up and running by running the command kubectl get pods and ensure all status is running, then execute ./example.sh"
echo "You can cleanup the kubernetes cluster by issuing the command ./deleteServices.sh"
echo "view any remaining resources using kubectl get all"
echo "Check that the services can communicate by performing the following command: kubectl exec -it \$DATA_POD curl config-service:8080/config/v1/status"




