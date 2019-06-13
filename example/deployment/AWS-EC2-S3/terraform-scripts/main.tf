resource "aws_instance" "palisade-instance" {
  ami             = "${data.aws_ami.amazon_linux.id}"
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



