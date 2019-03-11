#!/usr/bin/env bash

. ./example/deployment/bash-scripts/setScriptPath.sh
java -jar $EXAMPLESERVICES/example-rest-user-service/target/example-rest-user-service-*-executable.jar \
                -httpPort=8083 \
                -extractDirectory=.extract/User \
                -Dpalisade.rest.config.path=$PWD/example/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=user \
                -Dpalisade.properties.app.title=rest-user-service
