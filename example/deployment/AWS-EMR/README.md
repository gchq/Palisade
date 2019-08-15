# Amazon Web Services - Elastic MapReduce (EMR) Example

This example shows how Palisade can be run on an AWS - EMR cluster. It will create an AWS EMR cluster and install
Palisade on to them. You can then launch some Palisade examples which will demonstrate the use of Palisade within
a live Hadoop cluster. By default this guide will run a word count MapReduce example that tell you have many unique
tax code values and how many of each value is in the data.

For an overview of the example see [here](../../README.md).

**Note: These example are NOT eligible for AWS' "free-tier" usage! If you run these it will cost you a small amount of money.**

**Note 2: You must remember to DELETE your instance afterwards to avoid running up a large bill!**

### Prerequisites

1. In order to run this example you will also need to have [Terraform](https://www.terraform.io/) installed locally. **Currently
this requires Terraform 0.11 and does not work with Terraform 0.12. This will be addressed in a future release.**
2. You will need an AWS account subscription.

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
    # Set the access and secret key plus token OR the profile name, not both!
 
    "aws_access_key" = "<Your AWS subscription access key>"
    
    "aws_secret_key" = "<Your AWS subscription secret key>"
 
    "aws_session_token" = "<Your AWS subscription session token>"
     
    "aws_profile_name" = "<Your AWS profile name>"

    # Override the default eu-west-1 if you want
    # "aws_region" = ""
 
    "bucket_name" = "<Globally unique S3 bucket name>"
    
    "key_name" = "<Name of EC2 key pair instance for EMR cluster>"
    
    "pem_file" = "<Path to the private key (.pem file) for the above key pair>"
    
    "vpc_id" = "<ID of a VPC in AWS subscription>"
    
    "ingress_ip_range" = [ "1.2.3.4/32" , "5.6.7.8/32" ]
    
    "subnet_id" = "<ID of subnet that is in above VPC>" 

    "palisade_version" = "<version of palisade to deploy>"
    ```
    
    This needs to be populated with the values indicated above before the example will run.
    
    1. Your AWS session token, access key and secret key are specific to your AWS subscription. You can find information on where these are located [here](https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html).
        
        You can setup a named profile according to instructions [here](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-profiles.html).
        
        You should set **either** the access and secret keys plus session token **or** a profile name, not both. If you are using shared credentials, the profile name should be in the AWS shared credential file
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
    8. The `palisade_version` should be the version of palisade to be deployed which should be the same as the version you have built locally.
    9. **Optional**: Change the AWS region by uncommenting the `aws_region` key and setting a region name.
    10. **Optional**: Change the number of employee records generated from the default of `10` by adding the variable `number_of_employees_in_test_data`
    11. **Optional**: Change the number of files the records are split over from the default of `1` by adding the variable `number_of_files_to_split_test_data_over`

4. Run the script below to start creating instances on AWS and run the example. 

    ```./example/deployment/AWS-EMR/bash-scripts/runAWS-EMRExample.sh```
5. Answer `yes` when asked if Terraform may create AWS infrastructure.
**You will start to incur charges to your AWS subscription at this point!**
6. It will take several minutes to deploy the EMR cluster and run the Palisade example.
7. Once complete, you can SSH into the master node by finding Services -> EMR from the AWS console, then selecting the name of the EMR
    instance created by Palisade (something like "PalisadeExample") and then selecting the "SSH" link from the "Master Public DNS" section near the top.
    Follow the instructions to log in. Ensure you are logged in as the "hadoop" user.
8. Running the command below inside the SSH terminal should list the outputs from the MapReduce job:

   ```hdfs dfs -ls output```
   
   which should look something like:
   
    ```
    Found 2 items
    -rw-r--r--   1 hadoop hadoop          0 2019-07-11 14:02 output/_SUCCESS
    -rw-r--r--   1 hadoop hadoop         10 2019-07-11 14:02 output/part-r-00000
    ```

    Run the following to display the output file:
    
    ```hdfs dfs -text output/part-r-00000```
    
    which will produce output similar to this (your actual counts and values may differ):
    ```bash
    hdfs dfs -text output/part-r-00000
    11500L     10
    ```

    This result comes from 6 queries of the data acting under different users and different purposes where only one query of the data returned the tax code.
9. **IMPORTANT! Destroy your cluster! Ignoring this step will leave your EMR cluster running and costing you money!**

    Log out of your SSH terminal and run the following from your client machine:
    
    ```./example/deployment/AWS-EMR/bash-scripts/destroyTerraform.sh```

    Answer `yes` when asked to destroy the AWS infrastructure Terraform created.
    
    If you receive an error about failing to destroy the S3 bucket,
    you will have to perform this step manually. Just select Services -> S3 from the AWS console and then find the bucket that you named in the start
    of this example and tick the box to the left of it, select "Delete" and follow the instructions on screen. You may wish to re-run the command
    above to confirm to yourself everything has been cleaned up.
