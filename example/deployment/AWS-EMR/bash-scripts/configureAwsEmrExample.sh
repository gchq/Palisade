#!/bin/bash

java -cp /home/hadoop/jars/example-model-*-shaded.jar \
    -Dpalisade.rest.config.path=/home/hadoop/deploy_example/resources/configRest.json \
    uk.gov.gchq.palisade.example.config.ExampleConfigurator \
    hdfs://${HOSTNAME}/example_data/employee_file0.avro
