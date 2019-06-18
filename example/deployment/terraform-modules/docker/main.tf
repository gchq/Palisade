resource "null_resource" "install_docker" {

  connection {
    type        = "ssh"
    host = "${var.host_name}"
    user        = "ec2-user"
    private_key = "${var.key_file}"
    agent       = false
    timeout     = "30s"
  }

  # install and start docker
  provisioner "remote-exec" {
    inline = [
      "sudo yum update -y",
      "sudo amazon-linux-extras install docker -y",
      "sudo service docker start",
      "sudo usermod -a -G docker ec2-user"
    ]
  }
}

resource "random_integer" "ri" {
  min = 1
  max = 9
}

output "dummy_value" {
  # only here to ensure that docker module is run before deploy_example module
  #value = "${var.host_name}"
  value = "${random_integer.ri.result}"
}
