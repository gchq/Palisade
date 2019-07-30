resource "azurerm_resource_group" "rg-ip" {
  name     = "${var.resource_group_name}"
  location = "${var.resource_group_location}"
}

resource "azurerm_public_ip" "ip" {
  name                = "${var.ip_name}"
  location            = "${azurerm_resource_group.rg-ip.location}"
  resource_group_name = "${azurerm_resource_group.rg-ip.name}"
  allocation_method   = "Static"
}
