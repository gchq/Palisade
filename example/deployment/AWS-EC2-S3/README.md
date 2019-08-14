# AWS EC2 Example

Example Running on AWS EC2 Instances Accessing Data In An S3 Bucket

This example demonstrates different users querying an avro file that is stored in an S3 bucket that does not have public access.
The queries are run from an EC2 instance that does not have access to the S3 bucket, and an attempt to download the avro file from that EC2 machine will fail.
The queries access Palisade Rest endpoints hosted on a separate EC2 machine where Palisade is running. This second EC2 instance has been granted AWS IAM permissions to list the bucket and also to get, put and delete objects to/from the bucket.
When you run the example you will see the data has been redacted in line with the rules.
For an overview of the example see [here](../../README.md)

This example runs terraform scripts which will spin up an EC2 instance and install the Palisade services on it. This instance will run in the the Default VPC and default subnet.
Terraform will also create an S3 bucket and grant IAM permissions on the bucket to a role which is then granted to this EC2 instance running the palisade application.
Terraform will create a sample data file on this EC2 instance and then upload this file from the EC2 instance to the S3 bucket.

Terraform will then spin up a second EC2 instance and install the example on this second instance. This instance does not get granted access to the S3 bucket.
When Terraform runs the example on the second EC2 instance the example connects to the Rest endpoints of the palisade services running on the first EC2 instance and runs queries as the users Alice, Bob and Eve.
Palisade queries the data in the S3 bucket and returns data to the example client redacted accordinging to the appropiate policy rules.

##### PREREQUISITES
- Terraform
- Git 
- An AWS Account
- A key pair, with the public key on your AWS account and the private key available locally where you are running the script <runAWS-EC2_S3Example.sh>

To run this example follow these steps (from the root of the project):

1.  Compile the code:
```bash
  mvn clean install -P example
```
2.  Build the executable jars:
```bash
  ./example/deployment/local-jvm/bash-scripts/buildServices.sh
```
3.  Create the file terraform.tfvars in example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningPalisade with content based on this
```bash
    # Set the access and secret key plus token OR the profile name, not both!
 
    "aws_access_key" = "<Your AWS subscription access key>"
    
    "aws_secret_key" = "<Your AWS subscription secret key>"
 
    "aws_session_token" = "<Your AWS subscription session token>"
     
    "aws_profile_name" = "<Your AWS profile name>"

    # Override the default eu-west-1 if you want
    # "aws_region" = ""

    
  project = "PalisadeExample"
  owner = "your_name"
  aws_region = "eu-west-1"
  # Either set the profile name or the access key, secret key and token
  aws_access_key_id = "xxx"
  aws_secret_access_key = "xxx"
  aws_session_token = "xxx"
  #aws_profile_name = "xxx"
  bucket_name = "palisadeec2test"
  s3_endpoint = "s3-eu-west-1.amazonaws.com"
  key_name = "create a key and put its name here"
  pem_file = "/path/to/created/private/key.pem"
  ingress_ip_range = ["1.2.3.4/32", "2.3.4.5/32"]
  instance_name = "PalisadeExample"
  instance_type = "t2.large"
  data_file_name = "employee_file0.avro"
```

4. Create a symbolic link to the above terraform.tfvars file in example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningExample
```bash
  cd example/deployment/AWS-EC2-S3/terraform-scripts/InstanceRunningExample; ln -s ../InstanceRunningPalisade/terraform.tfvars terraform.tfvars; cd -
```

5. Run the Terraform to create the two EC2 instances, install and start up Palisade on the first instance, install the example on the second instance and run it.
```bash
  ./example/deployment/AWS-EC2-S3/bash-scripts/runAWS-EC2-S3Example.sh
```

After completion run terraform destroy to tear down the two EC2 instances:

1. Tear down the instance running the example
```bash
  ./example/deployment/AWS-EC2-S3/bash-scripts/destroyTerraform.sh
```

#### Running the Example
After the terraform scripts have run and the 2 instances are up and running, ssh on to the box using the following command:
```bash
ssh -i <Location of your .pem file> ec2-user@<public DNS of Example box>
```
A quick "ps ax | grep java" will show that no java services are running locally. Meaning that the example will using the Palisade services on the "Services" box.
To then run the example with the option to display the results based on different users or roles, run this command: 
```bash
$ export PALISADE_REST_CONFIG_PATH="/home/ec2-user/example/example-model/src/main/resources/configRest.json"
$ java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.client.ExampleSimpleClient <User> <filename> <PURPOSE> | /home/ec2-user/example/deployment/bash-scripts/formatOutput.sh
```
The example list of users, purposes and file locations are:
- Alice, Bob, Eve
- SALARY, DUTY_OF_CARE, STAFF_REPORT or no purpose by using: ""
- s3a://<bucket name>.<s3_endpoint>/employee_file0.avro

If you want to compare the different outputs of the Palisade service for different users and purposes, you can redirect the output to a text file, such as "> User-PURPOSE.txt", and then re-run the command with a change in user or purpose and redirect the output to a different file. 
You can then run:
```bash
diff file1.txt file2.txt
```

##### Running the fixed demo
To run the static demo, run this:
```bash
$ export PALISADE_REST_CONFIG_PATH="/home/<user>/example/example-model/src/main/resources/configRest.json"
$ java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.runner.RestExample s3a://palisadeec2test.s3-eu-west-1.amazonaws.com/employee_file0.avro | /home/ec2-user/example/deployment/bash-scripts/formatOutput.sh
```
 


###### TO-DO:
- [ ] Show the EC2 instance where the example runs trying and failing to access the data in the S3 bucket
