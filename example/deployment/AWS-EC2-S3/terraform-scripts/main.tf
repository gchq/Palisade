module "security_group" {
  source = "../../terraform-modules/security_group"       # base directory from which modules are referenced is example/deployment/AWS-EC2-S3/terraform-scripts
  ingress_ip_range  = "${var.ingress_ip_range}"
  owner             = "${var.owner}"
  project           = "${var.project}"
}

module "ami" {
  source = "../../terraform-modules/ami"
}

module "deploy_example" {
  source = "../../terraform-modules/deploy_example"
  key_file = "${var.pem_file}"
  host_name = "${aws_instance.palisade_instance.public_dns}"
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
  #  "${module.security_group.palisade_allow_inbound.sg_name}"
     "${aws_security_group.palisade_allow_inbound.id}"
  ]

  iam_instance_profile = "${aws_iam_instance_profile.palisade_instance_profile.id}"
}


