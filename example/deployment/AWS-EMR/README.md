# Amazon Web Services - Elastic MapReduce Example

This example shows how Palisade can be run on an AWS - EMR cluster. It will create some AWS instances and install
Palisade on to them. You can then launch some Palisade examples which will demonstrate the use of Palisade within
a live Hadoop cluster. The example will start a MapReduce job containing several queries on the example HR data that
will be processed through a simple MapReduce job.

For an overview of the example see [here](../../README.md).

**Note: These example are NOT eligible for AWS' "free-tier" usage! If you run these it will cost you a small amount of money.**

**Note 2: You must remember to DELETE your instance afterwards to avoid running up a large bill!**

### Prerequisites

1. In order to run this example you will also need to have [Terraform](https://www.terraform.io/) installed locally.
2. You will need and AWS account subscription.

### Instructions

This example requires some set up on the your part before it will run, due to having to be configured to run with your
AWS account.
 
To run the AWS-EMR example follow these steps (from the root of the project):

1. Compile the code:
    ```bash
    mvn clean install -P example
    ```
 
2.  Build the executable jars:
     ```bash
       ./example/deployment/local-jvm/bash-scripts/buildServices.sh
     ```


3. Create a Terraform variables file. Copy this template and save it as `example/deployment/AWS-EMR/terraform-scripts/terraform.tfvars`:

    ```bash
    "aws_access_key" = "<Your AWS subscription access key>"
    
    "aws_secret_key" = "<Your AWS subscription secret key"
    
    "bucket_name" = "<Globally unique S3 bucket name>"
    
    "key_name" = "<Name of EC2 key pair instance for EMR cluster>"
    
    "pem_file" = "<Path to the private key file for the above key pair>"
    
    "vpc_id" = "????"
    
    "ingress_ip_range" = ["35.176.136.170/32" , "35.177.97.88/32" , "35.178.132.230/32"]
    
    "subnet_id" = "????" 
    ```
    
    This needs to be populated with the values indicated above before the example will run.
    
    Notes:
    1. 
    2. 

    
4. Check keypair
5. Run script
6. SSH in and retrieve results from HDFS
7. Destroy cluster. Note the cost if you don't!
