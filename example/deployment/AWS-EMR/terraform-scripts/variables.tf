variable "project" {
  default = "Palisade Example"
}

variable "owner" {
  default = "Unknown"
}

variable "aws_region" {     
    default = "eu-west-1" 
} 

variable "aws_access_key" {
    default = ""
} 

variable "aws_secret_key" {
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

variable "cluster_name" {
  default = "PalisadeExample"
}

variable "release_label" {
  default = "emr-5.24.0"
}

variable "apps_to_install" {
  type = "list"
  default = [
  "Hadoop",
  "Zookeeper",
  "JupyterHub",
  "Ganglia",
  "Spark"
  ]
}

variable "master_instance_type" {
  default = "m4.xlarge"
}

variable "core_instance_type" {
  default = "m4.large"
}

variable "master_instance_count" {
  default = "1"
}

variable "core_instance_count" {
  default = "2"
}

variable "palisade_version" {}

variable "number_of_employees_in_test_data" {
  default = "100"
}

variable "number_of_files_to_split_test_data_over" {
  default = "2"
}