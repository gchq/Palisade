#!/usr/bin/env bash
kubectl delete -f ./persistent-volume/k8sPersistentVolume.yaml
kubectl delete -f ./persistent-volume/k8sPersistentVolumeClaim.yaml
