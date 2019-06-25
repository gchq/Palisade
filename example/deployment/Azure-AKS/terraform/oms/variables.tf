variable "resource_group_name" {
  type        = "string"
  description = "Name of the azure resource group."
}

variable "oms_location" {
  type        = "string"
  description = "Location of the azure resource group."
}

variable "oms_sku" {
  type        = "string"
  description = "The sku for the version of oms provisioned"
  # Default to free so this has to be overridden to provision paid sku
  default     = "Free"
}
