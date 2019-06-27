provider "aws" {
  access_key = "${var.aws_access_key}"
  secret_key = "${var.aws_secret_key}"
  region = "${var.aws_region}"
}

module "security_group" {
  source = "../../terraform-modules/security_group"     # base directory from which modules are referenced is example/deployment/AWS-EC2-S3/terraform-scripts
  ingress_ip_range = "${var.ingress_ip_range}"
  owner = "${var.owner}"
  project = "${var.project}"
}

module "ami" {
  source = "../../terraform-modules/ami"
}

module "iam" {
  source = "../../terraform-modules/iam"
  bucket_nme = "${var.bucket_name}"
}

module "s3_bucket" {
  source = "../../terraform-modules/s3_bucket"
  bucket_name = "${var.bucket_name}"
  owner = "${var.owner}"
  project = "${var.project}"
}

module "deploy_example" {
  source = "../../terraform-modules/deploy_example"
  key_file = "${var.pem_file}"
  host_name = "${aws_instance.palisade_instance.public_dns}"
  ec2_userid = "${var.ec2_userid}"
  data_file_name = "${var.data_file_name}"
  bucket_name = "${var.bucket_name}"
  s3_endpoint = "${var.s3_endpoint}"
}

resource "aws_instance" "palisade_instance" {
  ami = "${module.ami.ami_id}"
  instance_type = "${var.instance_type}"
  key_name = "${var.key_name}"

  tags {
    Name = "Palisade Example"
    Owner = "${var.owner}"
    Project = "${var.project}"
  }

  security_groups = [
    "${module.security_group.sg_name}"]

  iam_instance_profile = "${module.iam.instance_profile_id}"
}


