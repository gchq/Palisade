#!/usr/bin/env bash
AKS_PERS_RESOURCE_GROUP=$1
AKS_PERS_STORAGE_ACCOUNT_NAME=$2
AKS_CLUSTER_NAME=$3

STORAGE_KEY=$(az storage account keys list --resource-group $AKS_PERS_RESOURCE_GROUP --account-name $AKS_PERS_STORAGE_ACCOUNT_NAME --query "[0].value" -o tsv)
az aks get-credentials -n $AKS_CLUSTER_NAME -g $AKS_PERS_RESOURCE_GROUP
kubectl create secret generic data-share-secret --from-literal=azurestorageaccountname=$AKS_PERS_STORAGE_ACCOUNT_NAME --from-literal=azurestorageaccountkey=$STORAGE_KEY