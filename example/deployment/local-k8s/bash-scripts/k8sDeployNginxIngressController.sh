#!/usr/bin/env bash
DIR1=$1
if [[ -n "$DIR1" ]]; then
   kubectl apply -f $DIR1/ingress/mandatory.yaml
   kubectl apply -f $DIR1/ingress/cloudGeneric.yaml
else
   echo "argument error"
fi
