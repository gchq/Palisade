# Main terraform script to deploy Palisade on ec2, grant permissions via IAM to read from S3 bucket, and successfully demonstrate Palisade against  a file in an S3 bucket
provider "aws" {
  access_key = "${var.aws_access_key_id}"
  secret_key = "${var.aws_secret_access_key}"
  token = "${var.aws_session_token}"
  profile = "${var.aws_profile_name}"
  region = "${var.aws_region}"
}

module "security_group" {
  source = "../../../terraform-modules/security_group"     # base directory from which modules are referenced is example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningPalisade
  ingress_ip_range = "${var.ingress_ip_range}"
  owner = "${var.owner}"
  project = "${var.project}"
}

module "ami" {
  source = "../../../terraform-modules/ami"
}

module "iam" {
  source = "../../../terraform-modules/iam"
  bucket_name = "${var.bucket_name}"
}

module "s3_bucket" {
  source = "../../../terraform-modules/s3_bucket"
  bucket_name = "${var.bucket_name}"
  owner = "${var.owner}"
  project = "${var.project}"
}

# create hadoop_s3.xml from templatefile
data "template_file" "hadoop_s3" {
  template = "${file("../../../../resources/hadoop_s3.tmpl")}"
  vars = {
    bucket_name = "${var.bucket_name}"
  }
}
resource "local_file" "hadoop_s3" {
  content = "${data.template_file.hadoop_s3.rendered}"
  filename = "../../../../resources/hadoop_s3.xml"
}

# create instance running Palisade
resource "aws_instance" "palisade_instance" {
  ami = "${module.ami.ami_id}"
  instance_type = "${var.instance_type}"
  key_name = "${var.key_name}"

  tags {
    Name = "Running Palisade Services"
    Owner = "${var.owner}"
    Project = "${var.project}"
  }

  security_groups = [
    "${module.security_group.sg_name}"]

  iam_instance_profile = "${module.iam.instance_profile_id}"
}

module "deploy_palisade" {
  source = "../../../terraform-modules/deploy_palisade"
  key_file = "${var.pem_file}"
  host_name = "${aws_instance.palisade_instance.public_dns}"
  private_host_name = "${aws_instance.palisade_instance.private_dns}"
  ec2_userid = "${var.ec2_userid}"
  data_file_name = "${var.data_file_name}"
  bucket_name = "${var.bucket_name}"
  s3_endpoint = "s3-${var.aws_region}.amazonaws.com"
  palisade_version = "${var.palisade_version}"

}

output "palisade_host_private_host_name" {
  value = "${aws_instance.palisade_instance.private_dns}"
}

output "sgname" {
  value = "${module.security_group.sg_name}"
}

output "aws_region" {
  value = "${var.aws_region}"
}
