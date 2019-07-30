#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will deploy an instance of the Palisade REST Redirector service on the master node - listening on port 8084

export PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json

java -cp /home/hadoop/jars/example-rest-redirector-*-shaded.jar -Dpalisade.rest.basePath=http://0.0.0.0:8084/data/v1 uk.gov.gchq.palisade.redirect.service.Launcher
