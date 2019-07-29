#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will deploy an instance of the Palisade User service on the master node - listening on port 8083

export PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json

sudo PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json java -jar /home/hadoop/jars/example-rest-user-service-*-executable.jar \
    -httpPort=8083 \
    -extractDirectory=.extract/User \
    -Dpalisade.properties.app.title=rest-user-service
