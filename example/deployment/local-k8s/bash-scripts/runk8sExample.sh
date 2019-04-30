#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"
"$K8SBASHSCRIPTS/k8sDeployExample.sh"
sleep 5
EXAMPLE_POD=$(kubectl get pod -l job-name=configure-example -o jsonpath="{.items[0].metadata.name}")
kubectl logs -f $EXAMPLE_POD
