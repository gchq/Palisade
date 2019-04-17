#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will deploy an instance of the Palisade service on the master node - listening on port 8080

sudo java -jar /home/hadoop/jars/example-rest-palisade-service-*-executable.jar \
    -httpPort=8080 \
    -extractDirectory=.extract/Palisade \
    -Dpalisade.rest.config.path=/home/hadoop/deploy_example/resources/configRest.json \
    -Dpalisade.properties.app.title=rest-palisade-service