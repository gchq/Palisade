#!/bin/bash
export DIR=$(dirname "$0")
. "$DIR/../../bash-scripts/setScriptPath.sh"
kubectl apply -f ${K8SBASHSCRIPTS}/configure-example/k8sConfigureExample.yaml
sleep 5
export EXAMPLE_POD=$(kubectl get pod -l job-name=configure-example -o jsonpath="{.items[0].metadata.name}")
kubectl logs -f $EXAMPLE_POD
