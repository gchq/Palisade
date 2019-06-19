data "aws_vpc" "default" {
  default = true
}

resource "security_group" "palisade_allow_inbound" {
  name        = "palisade_allow_inbound"
  description = "Allow inbound traffic for Palisade"
  vpc_id      = "${data.aws_vpc.default.id}"

  tags {
    Name = "Palisade Security Group"
    Owner = "${var.owner}"
    Project = "${var.project}"
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

#output "sg_name" {
#  description = "The name of the security group"
#  value = aws_security_group.this.*.name
#}
