#!/usr/bin/env bash
DIR1=$1
if [[ -n "$DIR1" ]]; then
   kubectl delete -f $DIR1/configure-example/k8sConfigureExample.yaml
else
   echo "argument error"
fi
