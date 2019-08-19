resource "null_resource" "deploy_example" {

  # NOTE: the current directory when running this will be example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningPalisade ***********

  triggers = {
    update = 2
  }

  connection {
    type = "ssh"
    host = "${var.host_name}"
    user = "${var.ec2_userid}"
    private_key = "${file("${var.key_file}")}"
    agent = false
    timeout = "10m"
  }

  # install java
  provisioner "remote-exec" {
    inline = [
      "sudo yum update -y",
      "sudo amazon-linux-extras install java-openjdk11 -y",
      "sudo yum install dos2unix -y"
    ]
  }

  # Make required directories on the ec2 instance
  provisioner "remote-exec" {
    inline = [
      "mkdir -p /home/${var.ec2_userid}/example/deployment/bash-scripts",
      "mkdir -p /home/${var.ec2_userid}/example_logs",
      "mkdir -p /home/${var.ec2_userid}/example/example-model/src/main/resources",
      "mkdir -p /home/${var.ec2_userid}/example/example-model/target",
    ]
  }

  # Copy bash-scripts directories to ec2 instance......and make scripts executable
  provisioner "file" {
    source = "../../bash-scripts/"
    destination = "/home/${var.ec2_userid}/example/deployment/bash-scripts"
  }
  provisioner "file" {
    source = "../../../bash-scripts/"
    destination = "/home/${var.ec2_userid}/example/deployment/bash-scripts"
  }

  # Used to convert all dos files to unix format 
  provisioner "remote-exec" {
    inline = [
      "dos2unix /home/${var.ec2_userid}/example/deployment/bash-scripts/*.sh",
    ]
  }
  provisioner "remote-exec" {
    inline = [
      "chmod 774 /home/${var.ec2_userid}/example/deployment/bash-scripts/*.sh",
    ]
  }

  # Copy configRest.json and example-model jar to ec2 instance.....and edit configRest.json
  provisioner "file" {
    source = "../../../../example-model/src/main/resources/configRest.json"
    destination = "/home/${var.ec2_userid}/example/example-model/src/main/resources/configRest.json"
  }
  provisioner "remote-exec" {
    inline = [
      "ip=`hostname -I  | sed 's/ .*//'`",
      "sed -i \"s/localhost/${var.palisade_host_private_host_name}/\" /home/${var.ec2_userid}/example/example-model/src/main/resources/configRest.json"
    ]
  }
  provisioner "file" {
    source = "../../../../example-model/target/example-model-${var.palisade_version}-shaded.jar"
    destination = "/home/${var.ec2_userid}/example/example-model/target/example-model-${var.palisade_version}-shaded.jar"
  }
  provisioner "file" {
    source = "../../../../resources/hadoop_s3.xml"
    destination = "/home/${var.ec2_userid}/example/resources/hadoop_s3.xml"
  }

  # Run the Example...
  provisioner "remote-exec" {
    inline = [
      "/home/${var.ec2_userid}/example/deployment/bash-scripts/runFormattedEC2Example.sh Alice s3a://${var.bucket_name}.${var.s3_endpoint}/employee_file0.avro SALARY"
    ]
  }
}
