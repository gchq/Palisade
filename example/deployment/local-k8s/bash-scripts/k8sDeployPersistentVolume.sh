#!/usr/bin/env bash
kubectl apply -f ./persistent-volume/k8sPersistentVolume.yaml
kubectl apply -f ./persistent-volume/k8sPersistentVolumeClaim.yaml
