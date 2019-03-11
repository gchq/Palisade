#!/usr/bin/env bash

. ./example/deployment/bash-scripts/setScriptPath.sh
java -jar $EXAMPLESERVICES/example-rest-data-service/target/example-rest-data-service-*-executable.jar \
                -httpPort=8084 \
                -extractDirectory=.extract/Data \
                -Dpalisade.rest.config.path=$PWD/example/example-model/src/main/resources/configRest.json \
                -Dpalisade.properties.app.title=rest-data-service \
                -Dpalisade.rest.basePath=data
