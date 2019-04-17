#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will deploy an instance of the Palisade Policy service on the master node - listening on port 8081

sudo java -jar /home/hadoop/jars/example-rest-policy-service-*-executable.jar \
    -httpPort=8081 \
    -extractDirectory=.extract/Policy \
    -Dpalisade.rest.config.path=/home/hadoop/deploy_example/resources/configRest.json \
    -Dpalisade.properties.app.title=rest-policy-service
