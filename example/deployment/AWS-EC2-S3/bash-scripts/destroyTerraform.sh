#!/usr/bin/env bash

# get the host name and security group from file
cd example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningPalisade
palisadehost=$(grep palisade_host_private_host_name terraformoutput.txt | cut -d " " -f3)
securitygroup=$(grep sgname terraformoutput.txt | tr -c '[a-zA-Z0-9_]' ' ' | cut -d " " -f4)

cd ../InstanceRunningExample
terraform init
terraform destroy -var sg_name=palisade_allow_inbound -var palisade_host_private_host_name=${palisadehost}

cd ../InstanceRunningPalisade
terraform init
terraform destroy
rm terraformoutput.txt