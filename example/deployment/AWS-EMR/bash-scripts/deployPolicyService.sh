#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will deploy an instance of the Palisade Policy service on the master node - listening on port 8081

export PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json

sudo java -jar /home/hadoop/jars/example-rest-policy-service-*-executable.jar \
    -httpPort=8081 \
    -extractDirectory=.extract/Policy \
    -Dpalisade.properties.app.title=rest-policy-service
