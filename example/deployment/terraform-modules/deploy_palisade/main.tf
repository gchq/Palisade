resource "null_resource" "deploy_palisade" {

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
    timeout = "30s"
  }

  # install and start docker, install java
  provisioner "remote-exec" {
    inline = [
      "sudo yum update -y",
      "sudo amazon-linux-extras install docker -y",
      "sudo service docker start",
      "sudo usermod -a -G docker ${var.ec2_userid}",
      "sudo amazon-linux-extras install java-openjdk11 -y",
      "sudo yum install dos2unix -y"
    ]
  }

  # Make required directories on the ec2 instance
  provisioner "remote-exec" {
    inline = [
      "mkdir -p /home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts",
      "mkdir -p /home/${var.ec2_userid}/example/deployment/bash-scripts",
      "mkdir -p /home/${var.ec2_userid}/example_logs",
      "mkdir -p /home/${var.ec2_userid}/example_data",
      "mkdir -p /home/${var.ec2_userid}/example/example-services/example-rest-config-service/target",
      "mkdir -p /home/${var.ec2_userid}/example/example-model/target",
      "mkdir -p /home/${var.ec2_userid}/example/example-model/src/main/resources",
      "mkdir -p /home/${var.ec2_userid}/example/example-services/example-rest-resource-service/target",
      "mkdir -p /home/${var.ec2_userid}/example/example-services/example-rest-user-service/target",
      "mkdir -p /home/${var.ec2_userid}/example/example-services/example-rest-policy-service/target",
      "mkdir -p /home/${var.ec2_userid}/example/example-services/example-rest-palisade-service/target",
      "mkdir -p /home/${var.ec2_userid}/example/example-services/example-rest-data-service/target",
      "mkdir -p /home/${var.ec2_userid}/example/resources",
    ]
  }

  # Copy bash-scripts directories to ec2 instance......and make scripts executable
  provisioner "file" {
    source = "../../../bash-scripts/"
    destination = "/home/${var.ec2_userid}/example/deployment/bash-scripts"
  }
  provisioner "file" {
    source = "../../bash-scripts/"
    destination = "/home/${var.ec2_userid}/example/deployment/bash-scripts"
  }
  provisioner "file" {
    source = "../../../local-jvm/bash-scripts/"
    destination = "/home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts"
  }
  # Convert all dos line endings to unix
    provisioner "remote-exec" {
      inline = [
        "dos2unix /home/${var.ec2_userid}/example/deployment/bash-scripts/*.sh",
        "dos2unix /home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/*.sh",
      ]
    }
  provisioner "remote-exec" {
    inline = [
      "chmod 774 /home/${var.ec2_userid}/example/deployment/bash-scripts/*.sh",
      "chmod 774 /home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/*.sh",
    ]
  }
  # Copy configRest.json to ec2 instance
  provisioner "file" {
    source = "../../../../example-model/src/main/resources/configRest.json"
    destination = "/home/${var.ec2_userid}/example/example-model/src/main/resources/configRest.json"
  }

  provisioner "remote-exec" {
    inline = [
      "/home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/stopAllServices.sh",
      "/home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/startETCD.sh",
    ]
  }

  # Deploy the Palisade config service on the ec2 instance......1st copy over its jar....
  provisioner "file" {
    source = "../../../../example-services/example-rest-config-service/target/example-rest-config-service-${var.palisade_version}-executable.jar"
    destination = "/home/${var.ec2_userid}/example/example-services/example-rest-config-service/target/example-rest-config-service-${var.palisade_version}-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.key_file} -o 'StrictHostKeyChecking no' ${var.ec2_userid}@${var.host_name} 'nohup /home/${var.ec2_userid}/example/deployment/bash-scripts/waitForHost.sh ${var.private_host_name}:2379/health /home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/startConfigService.sh > /home/${var.ec2_userid}/example_logs/startConfigService.log 2>&1 &'"
  }

  # Tell the config service how the various Palisade services should be distributed - this configuration is stored in the Config service.....1st copy over the jar....and the S3 specific config file...
  provisioner "file" {
    source = "../../../../example-model/target/example-model-${var.palisade_version}-shaded.jar"
    destination = "/home/${var.ec2_userid}/example/example-model/target/example-model-${var.palisade_version}-shaded.jar"
  }
  provisioner "file" {
    source = "../../../../resources/hadoop_s3.xml"
    destination = "/home/${var.ec2_userid}/example/resources/hadoop_s3.xml"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.key_file} -o 'StrictHostKeyChecking no' ${var.ec2_userid}@${var.host_name} 'nohup /home/${var.ec2_userid}/example/deployment/bash-scripts/waitForHost.sh ${var.private_host_name}:2379/health /home/${var.ec2_userid}/example/deployment/bash-scripts/waitForHost.sh ${var.private_host_name}:8085/config/v1/status /home/${var.ec2_userid}/example/deployment/bash-scripts/configureRemoteServices.sh  > /home/${var.ec2_userid}/example_logs/configureRemoteServices.log 2>&1 &'"
  }

  # Deploy the Palisade Resource service on the ec2 instance...1st copy over the jar...
  provisioner "file" {
    source = "../../../../example-services/example-rest-resource-service/target/example-rest-resource-service-${var.palisade_version}-executable.jar"
    destination = "/home/${var.ec2_userid}/example/example-services/example-rest-resource-service/target/example-rest-resource-service-${var.palisade_version}-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.key_file} -o 'StrictHostKeyChecking no' ${var.ec2_userid}@${var.host_name} 'nohup /home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/startResourceService.sh > /home/${var.ec2_userid}/example_logs/startResourceService.log 2>&1 &'"
  }

  # Deploy the Palisade User service on the ec2 instance....1st copy over the jar....
  provisioner "file" {
    source = "../../../../example-services/example-rest-user-service/target/example-rest-user-service-${var.palisade_version}-executable.jar"
    destination = "/home/${var.ec2_userid}/example/example-services/example-rest-user-service/target/example-rest-user-service-${var.palisade_version}-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.key_file} -o 'StrictHostKeyChecking no' ${var.ec2_userid}@${var.host_name} 'nohup /home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/startUserService.sh > /home/${var.ec2_userid}/example_logs/startUserService.log 2>&1 &'"
  }

  # Deploy the example Palisade Policy service on the ec2 instance.....1st copy over the jar...
  provisioner "file" {
    source = "../../../../example-services/example-rest-policy-service/target/example-rest-policy-service-${var.palisade_version}-executable.jar"
    destination = "/home/${var.ec2_userid}/example/example-services/example-rest-policy-service/target/example-rest-policy-service-${var.palisade_version}-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.key_file} -o 'StrictHostKeyChecking no' ${var.ec2_userid}@${var.host_name} 'nohup /home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/startPolicyService.sh > /home/${var.ec2_userid}/example_logs/startPolicyService.log 2>&1 &'"
  }

  # Deploy the example Palisade service (co-ordinating service) on the ec2 instance.....1st copy over the jar...
  provisioner "file" {
    source = "../../../../example-services/example-rest-palisade-service/target/example-rest-palisade-service-${var.palisade_version}-executable.jar"
    destination = "/home/${var.ec2_userid}/example/example-services/example-rest-palisade-service/target/example-rest-palisade-service-${var.palisade_version}-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.key_file} -o 'StrictHostKeyChecking no' ${var.ec2_userid}@${var.host_name} 'nohup /home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/startPalisadeService.sh > /home/${var.ec2_userid}/example_logs/startPalisadeService.log 2>&1 &'"
  }

  # Generate a data file on the instance....1st copy over the jar...
  provisioner "remote-exec" {
    inline = [
      "java -cp /home/${var.ec2_userid}/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.hrdatagenerator.CreateData /home/${var.ec2_userid}/example_data 10 2 > /home/${var.ec2_userid}/example_logs/createDataFile.log 2>&1 ",
      "aws s3 cp --recursive /home/${var.ec2_userid}/example_data s3://${var.bucket_name}/data/ || echo Files not copied to S3 bucket"
    ]
  }

  # Deploy the example Palisade Data service on the ec2 instance.....1st copy over the jar...
  provisioner "file" {
    source = "../../../../example-services/example-rest-data-service/target/example-rest-data-service-${var.palisade_version}-executable.jar"
    destination = "/home/${var.ec2_userid}/example/example-services/example-rest-data-service/target/example-rest-data-service-${var.palisade_version}-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.key_file} -o 'StrictHostKeyChecking no' ${var.ec2_userid}@${var.host_name} 'nohup /home/${var.ec2_userid}/example/deployment/local-jvm/bash-scripts/startDataService.sh > /home/${var.ec2_userid}/example_logs/startDataService.log 2>&1 &'"
  }

  # Configure the Example - create some users and policies...
  provisioner "remote-exec" {
      inline = [
        "/home/${var.ec2_userid}/example/deployment/bash-scripts/waitForHost.sh http://${var.private_host_name}:8081/policy/v1/status /home/${var.ec2_userid}/example/deployment/bash-scripts/waitForHost.sh http://${var.private_host_name}:8082/resource/v1/status /home/${var.ec2_userid}/example/deployment/bash-scripts/waitForHost.sh http://${var.private_host_name}:8083/user/v1/status /home/${var.ec2_userid}/example/deployment/bash-scripts/configureExamples.sh s3a://${var.bucket_name}/data/ > /home/${var.ec2_userid}/example_logs/configureExamples.log 2>&1 ",
      ]
  }
}
