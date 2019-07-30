resource "aws_security_group" "palisade_allow_inbound" {
  name        = "palisade_allow_inbound_EMR"
  description = "Allow inbound traffic for Palisade"
  vpc_id = "${var.vpc_id}"

  tags {
    Name = "Palisade Security Group"
    Owner = "${var.owner}"
    Project = "{$var.project}"
  }

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["${var.ingress_ip_range}"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
