#!/usr/bin/env bash

# deploy the Palisade service on an ec2 instance
cd ./example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningPalisade
terraform init
terraform apply -input=false -auto-approve | tee terraformoutput.txt

if [ $? -le 0 ]
then
    # get the host name and security group from file
    palisadehost=$(grep palisade_host_private_host_name terraformoutput.txt | cut -d " " -f3)
    securitygroup=$(grep sgname terraformoutput.txt | tr -c '[a-zA-Z0-9_]' ' ' | cut -d " " -f4)
    aws_region=$(grep aws_region terraformoutput.txt | cut -d " " -f3)
    sed -i "s/eu-west-1/${aws_region}/g" ../../../../resources/hadoop_s3.tmpl


    # now run the example on a different ec2 instance
    cd ../InstanceRunningExample
    terraform init
    terraform apply -input=false -auto-approve -var sg_name=${securitygroup} -var palisade_host_private_host_name=${palisadehost}
fi

