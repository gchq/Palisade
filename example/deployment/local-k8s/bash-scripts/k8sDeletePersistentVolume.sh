#!/usr/bin/env bash
DIR1=$1
if [[ -n "$DIR1" ]]; then
   kubectl delete -f $DIR1/persistent-volume/k8sPersistentVolume.yaml
   kubectl delete -f $DIR1/persistent-volume/k8sPersistentVolumeClaim.yaml
else
   echo "argument error"
fi
