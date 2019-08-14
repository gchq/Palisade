#!/usr/bin/env bash

# deploy the Palisade service on an ec2 instance
cd ./example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningPalisade
terraform init
terraform apply -input=false -auto-approve | tee terraformoutput.txt

# get the host name and security group from file
palisadehost=$(grep palisade_host_private_host_name terraformoutput.txt | cut -d " " -f3)
securitygroup=$(grep sgname terraformoutput.txt | cut -d " " -f3)

# now run the example on a different ec2 instance
cd ../InstanceRunningExample
terraform init
terraform apply -input=false -auto-approve -var sg_name=palisade_allow_inbound -var palisade_host_private_host_name=${palisadehost}
