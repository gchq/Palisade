AKS_PERS_RESOURCE_GROUP="tmp-palisade-storage"
AKS_PERS_STORAGE_ACCOUNT_NAME="palisadestorage"

STORAGE_KEY=$(az storage account keys list --resource-group $AKS_PERS_RESOURCE_GROUP --account-name $AKS_PERS_STORAGE_ACCOUNT_NAME --query "[0].value" -o tsv)

kubectl create secret generic data-share-secret --from-literal=azurestorageaccountname=$AKS_PERS_STORAGE_ACCOUNT_NAME --from-literal=azurestorageaccountkey=$STORAGE_KEY