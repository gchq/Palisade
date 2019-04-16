#!/usr/bin/env bash

#user supplies KeyPair and other inputs when running a bash script
if [[ $# -lt 1 ]];
then
    echo "Usage: $0 KeyName"
    echo -e "\nUse Terraform to instantiate AWS EMR instance and run Palisade example on it; need to supply the full pathname of the pem file to use to connect to AWS"
    exit 1;
fi

#./example/deployment/local-jvm/bash-scripts/buildServices.sh                 # ensure that executables for the services are built before deploying

cd ./example/deployment/AWS-EMR/terraform-scripts
terraform apply \
    -var pem_file=$1