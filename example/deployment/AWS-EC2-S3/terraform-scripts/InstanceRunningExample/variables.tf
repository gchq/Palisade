variable "project" {
  default = "Palisade Example"
}

variable "owner" {
  default = "Unknown"
}

variable "aws_region" {
    default = "eu-west-1"
}

variable "aws_access_key_id" {
  default = ""
}

variable "aws_secret_access_key" {
  default = ""
}

variable "aws_session_token" {
  default = ""
}

variable "aws_profile_name" {
  default = ""
}

variable "bucket_name" {}

variable "key_name" {}

variable "pem_file" {}

variable "ingress_ip_range" {
  type = "list"
}

variable "instance_name" {
  default = "PalisadeExample"
}

variable "instance_type" {
  default = "t2.large"
}

variable "data_file_name" {
  default = "employee_file0.avro"
}

variable "ec2_userid" {
  default = "ec2-user"
}

variable "sg_name" {}

variable "palisade_host_private_host_name" {}

variable "palisade_version" {}

