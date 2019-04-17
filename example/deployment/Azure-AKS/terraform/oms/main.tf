provider "azurerm"{
    version = "~>1.5"
}

resource "azurerm_resource_group" "ops" {
  # include prefix and location within name for easier association
  name     = "${var.resource_group_name}"
  location = "${var.oms_location}"
}

resource "azurerm_log_analytics_workspace" "oms_workspace" {
  # include prefix and location within name for easier association
  name                = "${join("-", list(var.resource_group_name, var.oms_location, "loganalytics"))}"
  location            = "${azurerm_resource_group.ops.location}"
  resource_group_name = "${azurerm_resource_group.ops.name}"
  sku                 = "${var.oms_sku}"
}

resource "azurerm_log_analytics_solution" "oms_container_monitoring" {
  location            = "${azurerm_resource_group.ops.location}"
  resource_group_name = "${azurerm_resource_group.ops.name}"
  workspace_resource_id = "${azurerm_log_analytics_workspace.oms_workspace.id}"
  workspace_name        = "${azurerm_log_analytics_workspace.oms_workspace.name}"
  solution_name         = "ContainerInsights"

  plan {
    publisher = "Microsoft"
    product   = "OMSGallery/ContainerInsights"
  }
}
output "oms_workspace_resource_id" {
  # this will be used by other resources such as AKS to emit telemetry to OMS
  value = "${azurerm_log_analytics_workspace.oms_workspace.id}"
}