#!/usr/bin/env bash

cd ./example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningPalisade
terraform init
terraform destroy

# get the host name and security group from file
palisadehost=$(grep palisade_host_private_host_name terraformoutput.txt | cut -d " " -f3)
securitygroup=$(grep sgname terraformoutput.txt | cut -d " " -f3)

cd ../InstanceRunningExample
terraform init
terraform destroy -var sg_name=palisade_allow_inbound -var palisade_host_private_host_name=${palisadehost}

rm ../InstanceRunningPalisade/terraformoutput.txt