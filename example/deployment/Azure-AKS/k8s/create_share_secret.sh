#!/usr/bin/env bash
AKS_RESOURCE_GROUP=$1
AKS_PERS_STORAGE_ACCOUNT_NAME=$2
AKS_CLUSTER_NAME=$3
STORAGE_RESOURCE_GROUP=$4


#echo $STORAGE_RESOURCE_GROUP
#echo $AKS_PERS_STORAGE_ACCOUNT_NAME

STORAGE_KEY=$(az storage account keys list --resource-group $STORAGE_RESOURCE_GROUP --account-name $AKS_PERS_STORAGE_ACCOUNT_NAME --query "[0].value" -o tsv)

#echo "storage key set to"
#echo $STORAGE_KEY

# You can force delete of the shared secret with
# nigel@Azure:~$ kubectl delete secret data-share-secret

az aks get-credentials -n $AKS_CLUSTER_NAME -g $AKS_RESOURCE_GROUP

if kubectl get secret data-share-secret; then
    echo "Skipping task as the secret already exists"
else
    kubectl create secret generic data-share-secret --from-literal=azurestorageaccountname=$AKS_PERS_STORAGE_ACCOUNT_NAME --from-literal=azurestorageaccountkey=$STORAGE_KEY
#    kubectl get secret data-share-secret -o yaml
fi