#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will deploy an instance of the Palisade service on the master node - listening on port 8080

export PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json

sudo java -jar /home/hadoop/jars/example-rest-palisade-service-*-executable.jar \
    -httpPort=8080 \
    -extractDirectory=.extract/Palisade \
    -Dpalisade.properties.app.title=rest-palisade-service