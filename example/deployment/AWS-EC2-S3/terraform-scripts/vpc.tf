# got error creating vpc - so will use an existing one as a workaround for now
#resource "aws_vpc" "palisade_vpc" {
#  cidr_block           = "${var.vpc_cidr_block}"
#  enable_dns_hostnames = true
#
#  tags {
#    Name = "Palisade VPC"
#    Owner = "${var.owner}"
#    Project = "{$var.project}"
#  }
#}

#resource "aws_subnet" "palisade_subnet" {
#  #vpc_id     = "${aws_vpc.palisade_vpc.id}"
#  vpc_id = "${var.vpc_id}"
#  cidr_block = "S{var.subnet_cidr_block}"
#
#  tags {
#    Name = "Palisade Subnet"
#    Owner = "${var.owner}"
#    Project = "{$var.project}"
#  }
#}

# now using default vpc which already has an internet g/w
#resource "aws_internet_gateway" "palisade_gw" {
#  #vpc_id = "${aws_vpc.palisade_vpc.id}"
#  vpc_id = "${var.vpc_id}"
#
#  tags {
#    Name = "Palisade Internet G/W"
#    Owner = "${var.owner}"
#    Project = "{$var.project}"
#  }
#}

# now using default vpc which already has a route table
#resource "aws_route_table" "palisade_rt" {
#  #vpc_id = "${aws_vpc.palisade_vpc.id}"
#  vpc_id = "${var.vpc_id}"
#
#  route {
#    cidr_block = "0.0.0.0/0"
#    gateway_id = "${aws_internet_gateway.palisade_gw.id}"
#  }
#
#  tags {
#    Name = "Palisade Routing Table"
#    Owner = "${var.owner}"
#    Project = "{$var.project}"
#  }
#}

# now using default vpc which already has a route table
#resource "aws_main_route_table_association" "palisade_mrta" {
#  #vpc_id         = "${aws_vpc.palisade_vpc.id}"
#  vpc_id = "${var.vpc_id}"
#  route_table_id = "${aws_route_table.palisade_rt.id}"
#}

resource "aws_security_group" "palisade_allow_inbound" {
  name        = "palisade_allow_inbound"
  description = "Allow inbound traffic for Palisade"
  #vpc_id      = "${aws_vpc.palisade_vpc.id}"
  vpc_id = "${var.vpc_id}"
#  depends_on  = ["aws_subnet.palisade_subnet"]

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