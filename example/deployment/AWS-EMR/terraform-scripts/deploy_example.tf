resource "null_resource" "deploy_example" {

  # NOTE: the current directory when running this will be example/deployment/AWS-EMR/terraform-scripts ***********

  connection {
    type = "ssh"
    host = "${aws_emr_cluster.palisade_cluster.master_public_dns}"
    user = "hadoop"
    private_key = "${file(var.pem_file)}"
    agent = false
    timeout = "30s"
  }

  # Copy pem file to .ssh directory on EMR master.....and restrict its permissions
  provisioner "file" {
    source = "${var.pem_file}"
    destination = "/home/hadoop/.ssh/${basename(var.pem_file)}"
  }

  provisioner "remote-exec" {
    inline = [
      "chmod 600 /home/hadoop/.ssh/${basename(var.pem_file)}",
    ]
  }

  # Make required directories on the master
  provisioner "remote-exec" {
    inline = [
      "mkdir -p /home/hadoop/deploy_example",
      "mkdir -p /home/hadoop/deploy_example/resources",
      "mkdir -p /home/hadoop/jars",
      "mkdir -p /home/hadoop/example_logs",
      "mkdir -p /home/hadoop/example_data",
    ]
  }

  # Copy EMR bash-scripts directory to EMR master......and make scripts executable
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

  # Ensure the services are not running
  provisioner "remote-exec" {
    inline = [
      "sudo kill `ps -aef | grep example-rest-.*-service | grep -v grep | awk '{print $2}'` || echo Killed",
    ]
  }

  # Deploy the Palisade config service on the EMR master......1st copy over its jar....
  provisioner "file" {
    source = "../../../example-services/example-rest-config-service/target/example-rest-config-service-*-executable.jar"
    destination = "/home/hadoop/jars/"
  }

  provisioner "local-exec" {
    command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/deployConfigService.sh > /home/hadoop/example_logs/deployConfigService.log 2>&1 &'"
  }

  # Tell the config service how the various Palisade services should be distributed over the cluster - this configuration is stored in the Config service.....1st copy over the jar....
  provisioner "file" {
    source = "../../../example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar"
    destination = "/home/hadoop/jars/example-model-0.2.1-SNAPSHOT-shaded.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/configureDistributedServices.sh  > /home/hadoop/example_logs/configureDistributedServices.log 2>&1 &'"
  }


  # Deploy the Palisade Resource service on the EMR master node...1st copy over the jar...
  provisioner "file" {
    source = "../../../example-services/example-rest-resource-service/target/example-rest-resource-service-*-executable.jar"
    destination = "/home/hadoop/jars/"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/deployResourceService.sh > /home/hadoop/example_logs/deployResourceService.log 2>&1 &'"
  }

  # Deploy the Palisade User service on the EMR master node....1st copy over the jar....
  provisioner "file" {
    source = "../../../example-services/example-rest-user-service/target/example-rest-user-service-0.2.1-SNAPSHOT-executable.jar"
    destination = "/home/hadoop/jars/example-rest-user-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/deployUserService.sh > /home/hadoop/example_logs/deployUserService.log 2>&1 &'"
  }

  # Deploy the example Palisade Policy service on the EMR master node.....1st copy over the jar...
  provisioner "file" {
    source = "../../../example-services/example-rest-policy-service/target/example-rest-policy-service-0.2.1-SNAPSHOT-executable.jar"
    destination = "/home/hadoop/jars/example-rest-policy-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/deployPolicyService.sh > /home/hadoop/example_logs/deployPolicyService.log 2>&1 &'"
  }

  # Deploy the example Palisade service (co-ordinating service) on the EMR master node.....1st copy over the jar...
  provisioner "file" {
    source = "../../../example-services/example-rest-palisade-service/target/example-rest-palisade-service-0.2.1-SNAPSHOT-executable.jar"
    destination = "/home/hadoop/jars/example-rest-palisade-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/deployPalisadeService.sh > /home/hadoop/example_logs/deployPalisadeService.log 2>&1 &'"
  }

  # Deploy the rest redirector
  provisioner "file" {
    source = "../../../example-services/example-rest-redirector-service/target/example-rest-redirector-service-0.2.1-SNAPSHOT-shaded.jar"
    destination = "/home/hadoop/jars/example-rest-redirector-service-0.2.1-SNAPSHOT-shaded.jar"
  }
  provisioner "local-exec" {
    command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/deployRESTRedirectorService.sh > /home/hadoop/example_logs/deployRESTRedirectorService.log 2>&1 &'"
  }

  # Generate a data file on the cluster and put it into hdfs....1st copy over the jar...
  provisioner "file" {
    source = "../../../example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar"
    destination = "/home/hadoop/jars/example-model-0.2.1-SNAPSHOT-shaded.jar"
  }
  provisioner "remote-exec" {
    inline = [
      "hdfs dfs -rm -r /example_data || echo Deleted",
      "java -cp /home/hadoop/jars/example-model-*-shaded.jar uk.gov.gchq.palisade.example.hrdatagenerator.CreateData  /home/hadoop/example_data  10  1 > /home/hadoop/example_logs/createDataFile.log 2>&1 ",
      "hdfs dfs -mkdir /example_data",
      "hdfs dfs -put /home/hadoop/example_data/* /example_data/"
    ]
  }

  # Deploy the example Palisade Data service on the EMR master node.....1st copy over the jar...
  provisioner "file" {
    source = "../../../example-services/example-rest-data-service/target/example-rest-data-service-0.2.1-SNAPSHOT-executable.jar"
    destination = "/home/hadoop/jars/example-rest-data-service-0.2.1-SNAPSHOT-executable.jar"
  }
  provisioner "remote-exec" {
    inline = [
      "/home/hadoop/deploy_example/deployDataServices.sh /home/hadoop/.ssh/${basename(var.pem_file)} > /home/hadoop/example_logs/deployDataServices.log 2>&1 ",
    ]
  }

  # Configure the Example - create some users and policies...
  provisioner "remote-exec" {
    inline = [
      "/home/hadoop/deploy_example/configureAwsEmrExample.sh > /home/hadoop/example_logs/configureExample.log 2>&1 ",
    ]
  }

  # Run the Palisade mapreduce example runner....1st copy over the jar
  provisioner "file" {
    source = "../../../deployment/AWS-EMR/example-aws-emr-runner/target/example-aws-emr-runner-0.2.1-SNAPSHOT-shaded.jar"
    destination = "/home/hadoop/jars/example-aws-emr-runner-0.2.1-SNAPSHOT-shaded.jar"
  }
  provisioner "remote-exec" {
    inline = [
      "hdfs dfs -rm -r /user/hadoop/output || echo Deleted",
      "/home/hadoop/deploy_example/executeExample.sh > /home/hadoop/example_logs/exampleOutput.log 2>&1 ",
    ]
  }
}
