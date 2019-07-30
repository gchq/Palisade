resource "azurerm_resource_group" "rg-net" {
  name     = "${var.resource_group_name}"
  location = "${var.resource_group_location}"
}

resource "azurerm_virtual_network" "aks-network" {
  name                = "${var.network_name}"
  location            = "${azurerm_resource_group.rg-net.location}"
  resource_group_name = "${azurerm_resource_group.rg-net.name}"
  address_space       = ["10.1.0.0/16"]
}

resource "azurerm_subnet" "aks-subnet" {
  name                      = "aks-subnet"
  resource_group_name       =  "${azurerm_resource_group.rg-net.name}"
  address_prefix            = "10.1.0.0/24"
  virtual_network_name      = "${azurerm_virtual_network.aks-network.name}"
}
