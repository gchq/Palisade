variable "client_id" {
  type = "string"
  description = "Client ID of service principal."
}
variable "client_secret" {
  type = "string"
  description = "Password/secret of service principal."
}
variable "subscription_id" {
  type = "string"
  description = "Subscription ID"
}

variable "primary_region" {
  type = "string"
  description = "Primary region"
}

variable "ops_resource_group_name" {
  type = "string"
  description = "OPs resource group name"
}

variable "config_share_resource_group_name" {
  type = "string"
  description = "Config share resource group name"
}
variable "config_share_storage_account_name" {
  type = "string"
  description = "Config share storage account name"
}
variable "config_share_share_name" {
  type = "string"
  description = "Config share name"
}

variable "ip_resource_group_name" {
  type = "string"
  description = "IP address resource group name"
}
variable "ip_name" {
  type = "string"
  description = "IP name"
}


variable "vnet_resource_group_name" {
  type = "string"
  description = "Resource group name"
}
variable "vnet_name" {
  type = "string"
  description = "Network name"
}

variable "aks_resource_group_name" {
  type = "string"
  description = "Resource group name"
}
variable "aks_cluster_name" {
  type = "string"
  description = "AKS cluster name"
}
variable "dns_prefix" {
  type = "string"
  description = "AKS DNS prefix"
}

variable "acr_name" {
  type = "string"
  description = "Name of the ACR."
}
