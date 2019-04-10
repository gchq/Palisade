resource "azurerm_resource_group" "rg-acr" {
  name     = "${var.resource_group_name}"
  location = "${var.resource_group_location}"
}



resource "azurerm_container_registry" "acr" {
  name                     = "${var.acr_name}"
  resource_group_name      = "${azurerm_resource_group.rg-acr.name}"
  location                 = "${azurerm_resource_group.rg-acr.location}"
  sku                      = "Standard"
  admin_enabled            = true
}
