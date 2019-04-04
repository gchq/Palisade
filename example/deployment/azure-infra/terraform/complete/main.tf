module "oms" {
    source = "../oms"

    resource_group_name = "${var.ops_resource_group_name}"
    oms_location = "${var.primary_region}"
    oms_sku = "${var.oms_sku}"
}

module "acr" {
    source = "../acr"

    resource_group_name = "${var.ops_resource_group_name}"
    resource_group_location = "${var.primary_region}"

}

module "vnet" {
    source = "../vnet"

    resource_group_name="${var.vnet_resource_group_name}"
    resource_group_location="${var.primary_region}"
    network_name="${var.vnet_name}"
}

module "ingress-ip" {
    source = "../ingress-ip"

    resource_group_name="${var.ip_resource_group_name}"
    resource_group_location="${var.primary_region}"
    ip_name="${var.ip_name}"
}

module "aks" {
    source = "../aks"

    cluster_name="${var.aks_cluster_name}"
    dns_prefix="${var.dns_prefix}"
    resource_group_name="${var.aks_resource_group_name}"
    location="${var.primary_region}"

    client_id="${var.client_id}"
    client_secret="${var.client_secret}"
    subscription_id="${var.subscription_id}"

    log_analytics_workspace_id="${module.oms.oms_workspace_resource_id}"
    vnet_subnet_id="/subscriptions/${var.subscription_id}/resourceGroups/${var.vnet_resource_group_name}/providers/Microsoft.Network/virtualNetworks/${var.vnet_network_name}/subnets/aks-subnet"
}
