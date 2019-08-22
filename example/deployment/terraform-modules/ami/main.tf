data "aws_ami" "amazon_linux-2" {
  most_recent = true

  owners = ["amazon"]

  filter {
    name = "name"

    values = [
      "amzn2-ami-hvm-*-x86_64-gp2",
    ]
  }

  filter {
    name = "owner-alias"

    values = [
      "amazon",
    ]
  }
}

output "ami_id" {
  value = "${data.aws_ami.amazon_linux-2.id}"
}
