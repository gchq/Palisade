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
    # Set the access and secret key OR the profile name, not both!
 
    "aws_access_key" = "<Your AWS subscription access key>"
    
    "aws_secret_key" = "<Your AWS subscription secret key>"
     
    "aws_profile_name" = "<Your AWS profile name>"

    # Override the default eu-west-1 if you want
    # "aws_region" = ""
 
    "bucket_name" = "<Globally unique S3 bucket name>"
    
    "key_name" = "<Name of EC2 key pair instance for EMR cluster>"
    
    "pem_file" = "<Path to the private key (.pem file) for the above key pair>"
    
    "vpc_id" = "<ID of a VPC in AWS subscription>"
    
    "ingress_ip_range" = [ "1.2.3.4/32" , "5.6.7.8/32" ]
    
    "subnet_id" = "<ID of subnet that is in above VPC>" 
    ```
    
    This needs to be populated with the values indicated above before the example will run.
    
    1. Your AWS access key and secret key are specific to your AWS subscription. You can find information on where these are located [here](https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html).
        
        You should set the **either** the access and secret keys **or** the profile name, not both. The profile name should be in the AWS shared credential file
        `~/.aws/credentials` (or `%UserProfile%\.aws\credentials` on Windows).
                
    2. The `bucket_name` must be a globally unique S3 bucket name of your choosing. Bucket naming rules are [here](https://docs.aws.amazon.com/AmazonS3/latest/dev//BucketRestrictions.html#bucketnamingrules).
    3. You will need to create a key pair which allows you to SSH into the EMR cluster and for Terraform to provision the cluster:
        1. Create a key pair by logging in and from the AWS console, select Services -> EC2. Then select Key Pairs from the side menu.
        2. Click "Create Key Pair" which will ask for a name and give you a private certificate to download.
        
       Put the name of your key pair in the `key_name` field  as it appears in AWS (no `.pem` extension).
    
    4. `pem_file` should be the path to the private key create in previous step (ending in `.pem` extension).
    5. `vpc_id` is the ID of the Virtual Private Cloud the EMR cluster will connect to. From AWS console, select Services -> VPC, then choose Your VPCs from the side menu.
    You may create a new VPC for this example or use an existing one. Place the name in the `vpc_id` field.
    6. The `ingress_ip_range` is a list of public IPs that will be able to connect into the EMR cluster. This should be your public IP for your client machine.
    7. The `subnet_id` should be the ID of a valid subnet from the Subnets page of the VPC service in AWS. It should be part of the VPC named earlier.
    8. **Optional**: Change the AWS region by uncommenting the `aws_region` key and setting a region name.
4. Initialise Terraform by running the following line from the Palisade root directory:

    ```(cd example/deployment/AWS-EMR/terraform-scripts/ && terraform init)```
5. Run the script below to start creating instances on AWS and run the example. It requires the key pair PEM file from earlier. 

    ```./example/deployment/AWS-EMR/bash-scripts/runAWS-EMRExample.sh <path to PEM private key file>```
6. Answer `yes` when asked if Terraform may create AWS infrastructure.
**You will start to incur charges to your AWS subscription at this point!**
7. It will take several minutes to deploy the EMR cluster and run the Palisade example.
8. Once complete, you can SSH into the master node by finding Services -> EMR from the AWS console, then selecting the name of the EMR
    instance created by Palisade (something like "PalisadeExample") and then selecting the "SSH" link from the "Master Public DNS" section near the top.
    Follow the instructions to log in.
9. Running the command below inside the SSH terminal should list the outputs from the MapReduce job:

   ```hdfs dfs -ls output```
   
   which should look something like:
   
    ```
    Found 2 items
    -rw-r--r--   1 hadoop hadoop          0 2019-07-11 14:02 output/_SUCCESS
    -rw-r--r--   1 hadoop hadoop         10 2019-07-11 14:02 output/part-r-00000
    ```

    Run the following to display the output file:
    
    ```hdfs dfs -text output/part-r-00000```
10. **IMPORTANT! Destroy your cluster! Ignoring this step will leave your EMR cluster running and costing you money!**

    Log out of your SSH terminal and run the following from your client machine:
    
    ```./example/deployment/AWS-EMR/bash-scripts/destroyTerraform.sh```

    Answer `yes` when asked to destroy the AWS infrastructure Terraform created.
    
    If you receive an error about failing to destroy the S3 bucket,
    you will have to perform this step manually. Just select Services -> S3 from the AWS console and then find the bucket that you named in the start
    of this example and tick the box to the left of it, select "Delete" and follow the instructions on screen. You may wish to re-run the command
    above to confirm to yourself everything has been cleaned up.
