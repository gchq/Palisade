#!/usr/bin/env bash

#user supplies the KeyPair to connect to AWS
if [[ $# -lt 1 ]];
then
    echo "Usage: $0 KeyName"
    echo -e "\nUse Terraform to instantiate AWS EC2 instance and run Palisade example on it; need to supply the full pathname of the pem file to use to connect to AWS"
    exit 1;
fi

#./example/deployment/local-jvm/bash-scripts/buildServices.sh                 # ensure that executables for the services are built before deploying
>/var/tmp/terraformoutput
# deploy the Palisade service on an ec2 instance
cd ./example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningPalisade
terraform apply -input=false -auto-approve -var pem_file=$1 | tee /var/tmp/terraformoutput

# get the host name and security group from file
palisadehost=$(grep palisade_host_private_host_name /var/tmp/terraformoutput | cut -d " " -f3)
securitygroup=$(grep sgname /var/tmp/terraformoutput | cut -d " " -f3)

# now run the example on a different ec2 instance
cd ../InstanceRunningExample
terraform apply -input=false -auto-approve -var pem_file=$1 -var sg_name=palisade_allow_inbound -var palisade_host_private_host_name=${palisadehost}
