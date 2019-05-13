#!/usr/bin/env bash
DIR1=$1
if [[ -n "$DIR1" ]]; then
   kubectl apply -f $DIR1/palisade-service/k8sPalisadeService.yaml
else
   echo "argument error"
fi
