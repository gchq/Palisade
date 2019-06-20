resource "null_resource" "deploy_example" {

  # NOTE: the current directory when running this will be example/deployment/AWS-EC2-S3/terraform-scripts ***********

  triggers = {
    update = 2
  }

  connection {
    type = "ssh"
    host = "${var.host_name}"
    user = "ec2-user"
    private_key = "${file("${var.key_file}")}"
    agent = false
    timeout = "30s"
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

  # Copy pem file to .ssh directory on EC2 instance.....and restrict its permissions
  provisioner "file" {
    source = "${var.key_file}"
    destination = "/home/ec2-user/.ssh/${basename(var.key_file)}"
  }
  provisioner "remote-exec" {
    inline = [
      "chmod 600 /home/ec2-user/.ssh/${basename(var.key_file)}",
    ]
  }

  # Make required directories on the ec2 instance
  provisioner "remote-exec" {
    inline = [
      "mkdir -p /home/ec2-user/example/deployment/local-jvm/bash-scripts",
      "mkdir -p /home/ec2-user/example/deployment/bash-scripts",
      #"mkdir -p /home/ec2-user/deploy_example/resources",
      "mkdir -p /home/ec2-user/jars",
      "mkdir -p /home/ec2-user/example_logs",
      "mkdir -p /home/ec2-user/example_data",
      "mkdir -p /home/ec2-user/example/example-services/example-rest-config-service/target",
    ]
  }

  # Copy bash-scripts directories to ec2 instance......and make scripts executable
  provisioner "file" {
    source = "../bash-scripts/"
    destination = "/home/ec2-user/example/deployment/bash-scripts"
  }
  provisioner "file" {
    source = "../../local-jvm/bash-scripts/"
    destination = "/home/ec2-user/example/deployment/local-jvm/bash-scripts"
  }
  provisioner "remote-exec" {
    inline = [
      "chmod 774 /home/ec2-user/example/deployment/bash-scripts/*.sh",
      "chmod 774 /home/ec2-user/example/deployment/local-jvm/bash-scripts/*.sh",
    ]
  }

  # Deploy ETCD on the ec2 instance
  provisioner "remote-exec" {
    inline = [
      "/home/ec2-user/example/deploymant/local-jvm/bash-scripts/startETCD.sh > /home/ec2-user/example_logs/startETCD.log 2>&1",
    ]
  }

  #  # Run buildServices locally
  #  provisioner "local-exec" {
  #    command = "../../local-jvm/bash-scripts/buildServices.sh"
  #  }

  # Ensure the services are not running
  provisioner "remote-exec" {
    inline = [
      "sudo kill `ps -aef | grep example-rest-.*-service | grep -v grep | awk '{print $2}'` || echo Killed",
      "docker stop etcd-gcr-v3.3.12 || echo Killed",
    ]
  }

  # Deploy the Palisade config service on the ec2 instance......1st copy over its jar....
  provisioner "file" {
    source = "../../../example-services/example-rest-config-service/target/example-rest-config-service-0.2.1-SNAPSHOT-executable.jar"
    destination = "/home/ec2-user/example/example-services/example-rest-config-service/target/example-rest-config-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.key_file} -o 'StrictHostKeyChecking no' ec2-user@${var.host_name} 'nohup /home/ec2-user/example/deploymant/local-jvm/bash-scripts/startConfigService.sh > /home/ec2-user/example_logs/startConfigService.log 2>&1 &'"
  }

  #  # Tell the config service how the various Palisade services should be distributed - this configuration is stored in the Config service.....1st copy over the jar....
  #  provisioner "file" {
  #      source = "../../../example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar"
  #      destination = "/home/hadoop/jars/example-model-0.2.1-SNAPSHOT-shaded.jar"
  #  }
  #  provisioner "local-exec" {
  #      command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_instance.palisade_instance.master_public_dns} 'nohup /home/hadoop/deploy_example/configureDistributedServices.sh  > /home/hadoop/example_logs/configureDistributedServices.log 2>&1 &'"
  #    }


  #  # Deploy the Palisade Resource service on the ec2 instance...1st copy over the jar...
  #  provisioner "file" {
  #      source = "../../../example-services/example-rest-resource-service/target/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar"
  #      destination = "/home/hadoop/jars/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar"
  #  }
  #  provisioner "local-exec" {
  #    command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_instance.palisade_instance.master_public_dns} 'nohup /home/hadoop/deploy_example/deployResourceService.sh > /home/hadoop/example_logs/deployResourceService.log 2>&1 &'"
  #  }

  #  # Deploy the Palisade User service on the ec2 instance....1st copy over the jar....
  #  provisioner "file" {
  #      source = "../../../example-services/example-rest-user-service/target/example-rest-user-service-0.2.1-SNAPSHOT-executable.jar"
  #      destination = "/home/hadoop/jars/example-rest-user-service-0.2.1-SNAPSHOT-executable.jar"
  #  }
  #  provisioner "local-exec" {
  #      command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_instance.palisade_instance.master_public_dns} 'nohup /home/hadoop/deploy_example/deployUserService.sh > /home/hadoop/example_logs/deployUserService.log 2>&1 &'"
  #    }

  #  # Deploy the example Palisade Policy service on the ec2 instance.....1st copy over the jar...
  #  provisioner "file" {
  #      source = "../../../example-services/example-rest-policy-service/target/example-rest-policy-service-0.2.1-SNAPSHOT-executable.jar"
  #      destination = "/home/hadoop/jars/example-rest-policy-service-0.2.1-SNAPSHOT-executable.jar"
  #  }
  #  provisioner "local-exec" {
  #        command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_instance.palisade_instance.master_public_dns} 'nohup /home/hadoop/deploy_example/deployPolicyService.sh > /home/hadoop/example_logs/deployPolicyService.log 2>&1 &'"
  #      }

  #  # Deploy the example Palisade service (co-ordinating service) on the ec2 instance.....1st copy over the jar...
  #  provisioner "file" {
  #      source = "../../../example-services/example-rest-palisade-service/target/example-rest-palisade-service-0.2.1-SNAPSHOT-executable.jar"
  #      destination = "/home/hadoop/jars/example-rest-palisade-service-0.2.1-SNAPSHOT-executable.jar"
  #  }
  #  provisioner "local-exec" {
  #        command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_instance.palisade_instance.master_public_dns} 'nohup /home/hadoop/deploy_example/deployPalisadeService.sh > /home/hadoop/example_logs/deployPalisadeService.log 2>&1 &'"
  #      }

  #  # Generate a data file on the instance....1st copy over the jar...
  #  provisioner "file" {
  #      source = "../../../example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar"
  #      destination = "/home/hadoop/jars/example-model-0.2.1-SNAPSHOT-shaded.jar"
  #  }
  #  provisioner "remote-exec" {
  #      inline = [
  #        "hdfs dfs -rm -r /example_data || echo Deleted",
  #        "java -cp /home/hadoop/jars/example-model-*-shaded.jar uk.gov.gchq.palisade.example.hrdatagenerator.CreateData  /home/hadoop/example_data  10  1 > /home/hadoop/example_logs/createDataFile.log 2>&1 ",
  #        "hdfs dfs -mkdir /example_data",
  #        "hdfs dfs -put /home/hadoop/example_data/* /example_data/"
  #      ]
  #    }

  #  # Deploy the example Palisade Data service on the ec2 instance.....1st copy over the jar...
  #  provisioner "file" {
  #      source = "../../../example-services/example-rest-data-service/target/example-rest-data-service-0.2.1-SNAPSHOT-executable.jar"
  #      destination = "/home/hadoop/jars/example-rest-data-service-0.2.1-SNAPSHOT-executable.jar"
  #  }
  #  provisioner "remote-exec" {
  #      inline = [
  #        "/home/hadoop/deploy_example/deployDataServices.sh /home/hadoop/.ssh/${basename(var.pem_file)} > /home/hadoop/example_logs/deployDataServices.log 2>&1 ",
  #      ]
  #    }

  #  # Configure the Example - create some users and policies...
  #  provisioner "remote-exec" {
  #      inline = [
  #        "/home/hadoop/deploy_example/configureAwsEmrExample.sh > /home/hadoop/example_logs/configureExample.log 2>&1 ",
  #      ]
  #    }

  #  # Run the Palisade mapreduce example runner....1st copy over the jar
  #  provisioner "file" {
  #      source = "../../../deployment/AWS-EMR/example-aws-emr-runner/target/example-aws-emr-runner-0.2.1-SNAPSHOT-shaded.jar"
  #      destination = "/home/hadoop/jars/example-aws-emr-runner-0.2.1-SNAPSHOT-shaded.jar"
  #  }
  #  provisioner "remote-exec" {
  #      inline = [
  #        "hdfs dfs -rm -r /user/hadoop/output || echo Deleted",
  #        "/home/hadoop/deploy_example/executeExample.sh > /home/hadoop/example_logs/exampleOutput.log 2>&1 ",
  #      ]
  #    }
}