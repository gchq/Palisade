
variable "resource_group_name" {
  type        = "string"
  description = "Name of the azure resource group."
}

variable "resource_group_location" {
  type        = "string"
  description = "Location of the azure resource group."
}
variable "acr_sku" {
  type        = "string"
  description = "SKU for the container registry (Premium is the requirement for replication support)."
  default     = "Premium"
}