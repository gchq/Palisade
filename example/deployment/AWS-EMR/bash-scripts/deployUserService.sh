#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will deploy an instance of the Palisade User service on the master node - listening on port 8083

sudo java -jar /home/hadoop/jars/example-rest-user-service-*-executable.jar \
    -httpPort=8083 \
    -extractDirectory=.extract/User \
    -Dpalisade.rest.config.path=/home/hadoop/deploy_example/resources/configRest.json \
    -Dpalisade.properties.app.title=rest-user-service
