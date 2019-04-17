# Deploy ACR using Terraform
Currently reuses arm template due to deploy as the terraform acr provider does not support replication.  There is a [PR](https://github.com/terraform-providers/terraform-provider-azurerm/pull/2055) in progress for adding replication support in the terraform provider.  The arm template deployment can be removed after this PR is merged upstream.

Geo replication is a requirement for the deployment and ACR will be deployed into the `uksouth` and `ukwest` Azure regions


## Init terraform

```bash
cd terraform/acr
```

```bash
terraform init
```

## Create Terraform Plan

```bash
terraform plan --out acr.plan \
    --var "resource_group_name=oneweek-acr-rg" \
    --var "resource_group_location=uksouth" \
    --var "acr_name=oneweekacr" \
    --var "acr_replica_location=ukwest"
```

## Apply Terraform Plan

```bash
terraform apply acr.plan
```