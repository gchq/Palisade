module "ami" {
  # base directory is example/deployment/AWS-EC2-S3/terraform-scripts
  source = "../../terraform-modules/ami"
}

module "docker" {
  source = "../../terraform-modules/docker"
  host_name = "${aws_instance.palisade_instance.public_dns}"
  key_file = "${var.pem_file}"
}

module "deploy_example" {
  source = "../../terraform-modules/deploy_example"
  #stuff = "${module.docker.dummy_value}"
  #stuff2 = "${module.docker.dummy_value}"
  #key_file = "${file("${var.pem_file}")}"
  key_file = "${var.pem_file}"
}

resource "aws_instance" "palisade_instance" {
  ami             = "${module.ami.ami_id}"
  instance_type   = "${var.instance_type}"
  key_name        = "${var.key_name}"

  tags {
    Name = "Palisade Example"
    Owner = "${var.owner}"
    Project = "${var.project}"
  }

  security_groups = [
    "${aws_security_group.palisade_allow_inbound.name}"
  ]

  iam_instance_profile = "${aws_iam_instance_profile.palisade_instance_profile.id}"
}


