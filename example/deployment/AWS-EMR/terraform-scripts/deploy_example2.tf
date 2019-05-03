resource "null_resource" "deploy_example2" {

# NOTE: the current directory when running this will be example/deployment/AWS-EMR/terraform-scripts ***********

  connection {
    type        = "ssh"
    host = "${aws_emr_cluster.palisade_cluster.master_public_dns}"
    user        = "hadoop"
    private_key = "${file(var.pem_file)}"
    agent       = false
    timeout     = "30s"
  }

  # Run the Palisade mapreduce example runner....1st copy over the jar...
  provisioner "file" {
      source = "../../../deployment/AWS-EMR/example-aws-emr-runner/target/example-aws-emr-runner-0.2.1-SNAPSHOT-shaded.jar"
      destination = "/home/hadoop/jars/example-aws-emr-runner-0.2.1-SNAPSHOT-shaded.jar"
  }
  provisioner "remote-exec" {
      inline = [
        "java -cp /home/hadoop/jars/example-aws-emr-runner-*-shaded.jar -Dpalisade.rest.config.path=/home/hadoop/deploy_example/resources/configRest.json uk.gov.gchq.palisade.example.AwsEmrMapReduceExample /example_data/Employee_file0.avro /user/hadoop/output > /home/hadoop/example_logs/exampleOutput.log 2>&1 ",
      ]
    }
}
