resource "azurerm_resource_group" "k8s-resource-group" {
  name     = "${var.resource_group_name}"
  location = "${var.location}"
}

resource "azurerm_kubernetes_cluster" "k8s-cluster-name" {
  name                = "${var.cluster_name}"
  location            = "${azurerm_resource_group.k8s-resource-group.location}" # Ref. to the RG definition above.
  resource_group_name = "${azurerm_resource_group.k8s-resource-group.name}"     # Ref. to the RG definition above.
  dns_prefix          = "${var.dns_prefix}"

  agent_pool_profile {
    name            = "default"
    count           = "${var.agent_count}"
    vm_size         = "${var.vm_size}"
    os_type         = "${var.os_type}"
    os_disk_size_gb = "${var.os_disk_size_gb}"
    vnet_subnet_id  = "${var.vnet_subnet_id}"
  }

  service_principal {
    client_id     = "${var.client_id}"
    client_secret = "${var.client_secret}"
  }

  addon_profile {
    oms_agent = {
      enabled                    = "true"
      log_analytics_workspace_id = "${var.log_analytics_workspace_id}"
    }
  }
}
