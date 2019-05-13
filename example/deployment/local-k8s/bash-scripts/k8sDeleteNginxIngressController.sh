#!/usr/bin/env bash
DIR1=$1
if [[ -n "$DIR1" ]]; then
   kubectl delete -f $DIR1/ingress/mandatory.yaml
   kubectl delete -f $DIR1/ingress/cloudGeneric.yaml
else
   echo "argument error"
fi
