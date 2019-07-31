#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will deploy an instance of the Palisade Resource service on the master node - listening on port 8082

sudo sed -i "s/localhost/${HOSTNAME}/g" /home/hadoop/deploy_example/resources/configRest.json

sudo PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json \
    java -jar /home/hadoop/jars/example-rest-resource-service-*-executable.jar \
    -httpPort=8082 \
    -extractDirectory=.extract/Resource \
    -Dpalisade.properties.app.title=rest-resource-service
