project = "PalisadeExample"
owner = "developer6959"
aws_region = "eu-west-1"
aws_access_key = "xxx"
aws_secret_key = "xxx"
bucket_name = "developer6959palisadetest"
key_name = "developer6959ireland"
vpc_id = "vpc-cd2d21a9"
subnet_id = "subnet-c584d79d"
vpc_cidr_block = "168.31.0.0/16"
subnet_cidr_block = "168.31.0.0/20"
cluster_name = "PalisadeExample"
release_label = "emr-5.22.0"
apps_to_install = [
  "Hadoop",
  "Zookeeper",
  "JupyterHub",
  "Ganglia",
  "Spark"
  ]
master_instance_type = "m4.xlarge"
core_instance_type = "m4.large"
master_instance_count = "1"
core_instance_count = "3"
