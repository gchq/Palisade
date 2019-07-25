#!/bin/bash

#make sure nothing is running
sudo kill `ps -aef | grep example-rest-.*-service | grep -v grep | awk '{print $2}'` || echo Killed

#clear out the jars directory
rm /home/hadoop/jars/*
cd /mnt1/repos/Palisade
sudo rm -Rf .extract
mkdir .extract

# Deploy the Palisade config service on the EMR master
cp example/example-services/example-rest-config-service/target/example-rest-config-service-*-executable.jar  /home/hadoop/jars/
nohup /home/hadoop/deploy_example/deployConfigService.sh > /home/hadoop/example_logs/deployConfigService.log 2>&1 &
echo deployed Palisade config service

# Tell the config service how the various Palisade services should be distributed over the cluster - this configuration is stored in the Config service.....1st copy over the jar....
cp example/example-model/target/example-model-*-shaded.jar  /home/hadoop/jars/
nohup /home/hadoop/deploy_example/configureDistributedServices.sh  > /home/hadoop/example_logs/configureDistributedServices.log 2>&1 &
echo configured Palisade config service

# Deploy the Palisade Resource service on the EMR master node...1st copy over the jar...
cp example/example-services/example-rest-resource-service/target/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar  /home/hadoop/jars/example-rest-resource-service-0.2.1-SNAPSHOT-executable.jar
nohup /home/hadoop/deploy_example/deployResourceService.sh > /home/hadoop/example_logs/deployResourceService.log 2>&1 &
echo deployed Palisade resource service

# Deploy the Palisade User service on the EMR master node....1st copy over the jar....
cp example/example-services/example-rest-user-service/target/example-rest-user-service-0.2.1-SNAPSHOT-executable.jar  /home/hadoop/jars/example-rest-user-service-0.2.1-SNAPSHOT-executable.jar
nohup /home/hadoop/deploy_example/deployUserService.sh > /home/hadoop/example_logs/deployUserService.log 2>&1 &
echo deployed Palisade user service

# Deploy the example Palisade Policy service on the EMR master node.....1st copy over the jar...
cp example/example-services/example-rest-policy-service/target/example-rest-policy-service-0.2.1-SNAPSHOT-executable.jar  /home/hadoop/jars/example-rest-policy-service-0.2.1-SNAPSHOT-executable.jar
nohup /home/hadoop/deploy_example/deployPolicyService.sh > /home/hadoop/example_logs/deployPolicyService.log 2>&1 &
echo deployed Palisade policy service

# Deploy the example Palisade service (co-ordinating service) on the EMR master node.....1st copy over the jar...
cp example/example-services/example-rest-palisade-service/target/example-rest-palisade-service-0.2.1-SNAPSHOT-executable.jar  /home/hadoop/jars/example-rest-palisade-service-0.2.1-SNAPSHOT-executable.jar
nohup /home/hadoop/deploy_example/deployPalisadeService.sh > /home/hadoop/example_logs/deployPalisadeService.log 2>&1 &
echo deployed Palisade Palisade service

# Deploy the example Palisade Data service on the EMR master node.....1st copy over the jar...
cp example/example-services/example-rest-data-service/target/example-rest-data-service-0.2.1-SNAPSHOT-executable.jar  /home/hadoop/jars/example-rest-data-service-0.2.1-SNAPSHOT-executable.jar
/home/hadoop/deploy_example/deployDataServices.sh /home/hadoop/.ssh/developer6959ireland.pem > /home/hadoop/example_logs/deployDataServices.log 2>&1
echo deployed Palisade data service

# Configure the Example - create some users and policies...
/home/hadoop/deploy_example/configureAwsEmrExample.sh > /home/hadoop/example_logs/configureExample.log 2>&1
echo configured example

# Run the Palisade mapreduce example runner....1st copy over the jar
cp example/deployment/AWS-EMR/example-aws-emr-runner/target/example-aws-emr-runner-0.2.1-SNAPSHOT-shaded.jar  /home/hadoop/jars/example-aws-emr-runner-0.2.1-SNAPSHOT-shaded.jar
hdfs dfs -rm -r /user/hadoop/output || echo Deleted
/home/hadoop/deploy_example/executeExample.sh > /home/hadoop/example_logs/exampleOutput.log 2>&1
