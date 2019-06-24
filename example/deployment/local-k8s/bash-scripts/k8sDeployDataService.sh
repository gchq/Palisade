#!/usr/bin/env bash
DIR1=$1
if [[ -n "$DIR1" ]]; then
   kubectl apply -f $DIR1/data-service/k8sDataService.yaml
else
   echo "argument error"
fi
