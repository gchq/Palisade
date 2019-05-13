#!/usr/bin/env bash
DIR1=$1
if [[ -n "$DIR1" ]]; then
   kubectl delete -f $DIR1/etcd/k8sConfigureServices.yaml
else
   echo "argument error"
fi
