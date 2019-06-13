variable "project" {
  default = "Palisade Example"
}

variable "owner" {
  default = "Unknown"
}

variable "aws_region" {
    default = "eu-west-1"
}

variable "aws_access_key" {}

variable "aws_secret_key" {}

variable "bucket_name" {}

variable "key_name" {}

variable "pem_file" {}

variable "vpc_id" {}

variable "vpc_cidr_block" {
  default = "168.31.0.0/16"
}

variable "subnet_id" { }

variable "subnet_cidr_block" {
  default = "168.31.0.0/20"
}

variable "ingress_ip_range" {
  type = "list"
}

variable "instance_name" {
  default = "PalisadeExample"
}

variable "instance_type" {
  default = "t2.large"
}
