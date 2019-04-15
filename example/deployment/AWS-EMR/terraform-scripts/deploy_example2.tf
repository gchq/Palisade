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

  # tell the config service how the various Palisade services should be distributed over the cluster - this is stored in the Config service
    provisioner "local-exec" {
      command = "ssh -f -i ${var.pem_file} -o 'StrictHostKeyChecking no' hadoop@${aws_emr_cluster.palisade_cluster.master_public_dns} 'nohup /home/hadoop/deploy_example/configureDistributedServices.sh ${aws_emr_cluster.palisade_cluster.master_public_dns}> /home/hadoop/example_logs/configureDistributedServices.log 2>&1 &'"
    }



}
