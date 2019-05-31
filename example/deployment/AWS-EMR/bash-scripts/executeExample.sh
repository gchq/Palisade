#!/bin/bash

export PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json

java -cp /home/hadoop/jars/example-aws-emr-runner-*-shaded.jar \
    uk.gov.gchq.palisade.example.AwsEmrMapReduceExample \
    hdfs://${HOSTNAME}/example_data/employee_file0.avro hdfs:///user/hadoop/output