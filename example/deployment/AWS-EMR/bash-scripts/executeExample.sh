#!/bin/bash

# Resolve version
SHADE_JAR=$(ls -1 /home/hadoop/jars/example-aws-emr-runner-*-SNAPSHOT-shaded.jar)

YARN_USER_CLASSPATH=$SHADE_JAR \
YARN_USER_CLASSPATH_FIRST=1 \
PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json yarn jar /home/hadoop/jars/example-aws-emr-runner-*-shaded.jar \
    uk.gov.gchq.palisade.example.AwsEmrMapReduceExample \
    hdfs://${HOSTNAME}/example_data/employee_file0.avro /user/hadoop/output

#TODO REMOVE THIS
#YARN_USER_CLASSPATH=/data/example/deployment/AWS-EMR/example-aws-emr-runner/target/example-aws-emr-runner-0.21-SNAPSHOT-shaded.jar YARN_USER_CLASSPATH_FIRST=1 PALISADE_REST_CONFIG_PATH=/data/example/deployment/AWS-EMR/bash-scripts/resources/local.json yarn jar /data/example/deployment/AWS-EMR/example-aws-emr-runner/target/example-aws-emr-runner-0.2.1-SNAPSHOT-shaded.jar uk.gov.gchq.palisade.example.AwsEmrMapReduceExample /data/employee_file0.avro  /rubbish

#docker run -it --rm -v $(pwd):/data --network=example_palisade_network -d harisekhon/hadoop:2.8

#docker exec -it