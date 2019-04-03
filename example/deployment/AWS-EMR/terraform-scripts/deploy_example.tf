resource "null_resource" "deploy_example" {

# NOTE: the current directory when running this will be example/deployment/AWS-EMR/terraform-scripts ***********

  connection {
    type        = "ssh"
    host = "${aws_emr_cluster.palisade_cluster.master_public_dns}"
    user        = "hadoop"
    private_key = "${file(var.pem_file)}"
    agent       = false
    timeout     = "30s"
  }

  # Copy pem file to .ssh directory on EMR master.....and restrict its permissions
  provisioner "file" {
      source      = "${var.pem_file}"
      destination = "/home/hadoop/.ssh/${basename(var.pem_file)}"
  }
  provisioner "remote-exec" {
    inline = [
      "chmod 600 /home/hadoop/.ssh/${basename(var.pem_file)}",
    ]
  }

  # Copy EMR bash-scripts directory to EMR master......and make scripts executable
  provisioner "remote-exec" {
    inline = [
      "mkdir -p /home/hadoop/deploy_example",
    ]
  }
  provisioner "file" {
      source = "../bash-scripts/"
      destination = "/home/hadoop/deploy_example"
  }
  provisioner "remote-exec" {
    inline = [
      "chmod 774 /home/hadoop/deploy_example/*.sh",
    ]
  }

  # Deploy ETCD on the core nodes
  provisioner "remote-exec" {
    inline = [
      "/home/hadoop/deploy_example/deployETCD.sh /home/hadoop/.ssh/${basename(var.pem_file)}",
    ]
  }

#  # Run buildServices locally
#  provisioner "local-exec" {
#    command = "../../local-jvm/bash-scripts/buildServices.sh"
#  }

  # Copy the executable jar files created by buildServices to the EMR master......
  provisioner "remote-exec" {
    inline = [
      "mkdir -p /home/hadoop/jars",
    ]
  }
  provisioner "file" {
      #source = "../../../example-services/example-rest-resource-service/target/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar"
      #source = "../bash-scripts/"
      #source = "../../../example-services/pom.xml"
      #destination = "/home/hadoop/jars/basename(../../../example-services/example-rest-resource-service/target/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar)"
      #/destination = "/home/hadoop/jars/${basename(../../../example-services/pom.xml}"
      source = "../../../example-services/example-rest-resource-service/target/"
      destination = "/home/hadoop/jars"
      #source = "../../../example-services/example-rest-resource-service/target/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar"
      #destination = "/home/hadoop/jars/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar"
  }



}




