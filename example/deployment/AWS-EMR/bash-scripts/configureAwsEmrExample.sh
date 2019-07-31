#!/bin/bash

private_dns=`hostname -f`

export PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json

java -cp /home/hadoop/jars/example-model-*-shaded.jar \
    uk.gov.gchq.palisade.example.config.ExampleConfigurator \
    hdfs://${private_dns}/example_data
