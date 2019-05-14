#!/bin/bash

java -cp /home/hadoop/jars/example-aws-emr-runner-*-shaded.jar \
    -Dpalisade.rest.config.path=/home/hadoop/deploy_example/resources/configRest.json \
    uk.gov.gchq.palisade.example.AwsEmrMapReduceExample \
    hdfs://${HOSTNAME}/example_data/employee_file0.avro hdfs:///user/hadoop/output