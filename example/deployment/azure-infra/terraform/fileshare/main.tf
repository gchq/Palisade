resource "azurerm_resource_group" "fileshare-rg" {
  name     = "${var.resource_group_name}"
  location = "${var.location}"
}

resource "azurerm_storage_account" "storage" {
  name                     = "${var.storage_account_name}"
  resource_group_name      = "${azurerm_resource_group.fileshare-rg.name}"
  location                 = "${var.location}"
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_storage_share" "share" {
  name = "${var.share_name}"
  resource_group_name  = "${azurerm_resource_group.fileshare-rg.name}"
  storage_account_name = "${azurerm_storage_account.storage.name}"
  quota = 50
}