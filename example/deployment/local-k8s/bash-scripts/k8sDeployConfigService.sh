#!/usr/bin/env bash
DIR1=$1
if [[ -n "$DIR1" ]]; then
   kubectl apply -f $DIR1/config-service/k8sConfigService.yaml
else
   echo "argument error"
fi
