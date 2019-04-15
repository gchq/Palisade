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
      source = "../../../example-services/example-rest-config-service/target/example-rest-config-service-0.2.1-SNAPSHOT-executable.jar"
      destination = "/home/hadoop/jars/example-rest-config-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "file" {
      source = "../../../example-services/example-rest-resource-service/target/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar"
      destination = "/home/hadoop/jars/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "file" {
      source = "../../../example-services/example-rest-user-service/target/example-rest-user-service-0.2.1-SNAPSHOT-executable.jar"
      destination = "/home/hadoop/jars/example-rest-user-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "file" {
      source = "../../../example-services/example-rest-policy-service/target/example-rest-policy-service-0.2.1-SNAPSHOT-executable.jar"
      destination = "/home/hadoop/jars/example-rest-policy-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "file" {
      source = "../../../example-services/example-rest-palisade-service/target/example-rest-palisade-service-0.2.1-SNAPSHOT-executable.jar"
      destination = "/home/hadoop/jars/example-rest-palisade-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "file" {
      source = "../../../example-services/example-rest-data-service/target/example-rest-data-service-0.2.1-SNAPSHOT-executable.jar"
      destination = "/home/hadoop/jars/example-rest-data-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "file" {
      source = "../../../example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar"
      destination = "/home/hadoop/jars/example-model-0.2.1-SNAPSHOT-shaded.jar"
  }

  # Deploy the Palisade config service on the EMR master
  provisioner "remote-exec" {
    inline = [
      "mkdir -p /home/hadoop/example_logs",
    ]
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/deployConfigService.sh > /home/hadoop/example_logs/deployConfigService.log 2>&1 &'"
  }

  # tell the config servive how the various Palisade services should be distributed over the cluster - this is stored in the Config service
    provisioner "local-exec" {
      command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/configureDistributedServices.sh ${aws_emr_cluster.palisade_cluster.master_public_dns}> /home/hadoop/example_logs/configureDistributedServices.log 2>&1 &'"
    }



}
