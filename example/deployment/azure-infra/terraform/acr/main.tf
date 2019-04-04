resource "azurerm_resource_group" "rg-acr" {
  name     = "${var.resource_group_name}"
  location = "${var.resource_group_location}"
}

resource "azurerm_template_deployment" "acr" {
  name                = "acctesttemplate-01"
  resource_group_name = "${azurerm_resource_group.rg-acr.name}"
  template_body = "${file("${path.module}/../../arm/acr/template.json")}" 

   # these key-value pairs are passed into the ARM Template's `parameters` block
  parameters {
    "acrName" = "${replace(join("", list(var.resource_group_name, azurerm_resource_group.rg-acr.location, "acr")), "-", "")}"
    "acrLocation" = "${azurerm_resource_group.rg-acr.location}"
    "acrSku" = "${var.acr_sku}"
    "acrReplicaLocation" = "${var.acr_replica_location}"
  }

  deployment_mode = "Incremental"
}

output "login_server" {
  value = "${azurerm_template_deployment.acr.outputs["acrLoginServer"]}"
}
