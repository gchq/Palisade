# Main terraform script to deploy the Palisade example on an ec2 instance, grant permissions via IAM to read from S3 bucket, and successfully demonstrate Palisade against  a file in an S3 bucket
provider "aws" {
  access_key = "${var.aws_access_key_id}"
  secret_key = "${var.aws_secret_access_key}"
  token = "${var.aws_session_token}"
  region = "${var.aws_region}"
}

module "ami" {
  source = "../../../terraform-modules/ami"
}

# create instance running Example
resource "aws_instance" "instance_running_palisade_example" {
  ami = "${module.ami.ami_id}"
  instance_type = "${var.instance_type}"
  key_name = "${var.key_name}"

  tags {
    Name = "Running Palisade Example"
    Owner = "${var.owner}"
    Project = "${var.project}"
  }

  security_groups = [
    "${var.sg_name}"]
}

module "deploy_example" {
  source = "../../../terraform-modules/deploy_example"
  key_file = "${var.pem_file}"
  host_name = "${aws_instance.instance_running_palisade_example.public_dns}"
  private_host_name = "${aws_instance.instance_running_palisade_example.private_dns}"
  ec2_userid = "${var.ec2_userid}"
  bucket_name = "${var.bucket_name}"
  s3_endpoint = "${var.s3_endpoint}"
  palisade_host_private_host_name = "${var.palisade_host_private_host_name}"
}
