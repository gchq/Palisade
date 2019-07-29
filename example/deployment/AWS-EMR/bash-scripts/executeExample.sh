#!/bin/bash

private_dns=`hostname -f`

# Resolve version
SHADE_JAR=$(ls -1 /home/hadoop/jars/example-aws-emr-runner-*-SNAPSHOT-shaded.jar)

hdfs dfs -rm -r /user/hadoop/output

YARN_USER_CLASSPATH=$SHADE_JAR \
YARN_USER_CLASSPATH_FIRST=1 \
PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json yarn jar /home/hadoop/jars/example-aws-emr-runner-*-shaded.jar \
    uk.gov.gchq.palisade.example.AwsEmrMapReduceExample \
    hdfs://${private_dns}/example_data /user/hadoop/output
