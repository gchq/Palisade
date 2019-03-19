h#!/usr/bin/env bash

. ./example/deployment/bash-scripts/setScriptPath.sh
java -jar $EXAMPLESERVICES/example-rest-palisade-service/target/example-rest-palisade-service-*-executable.jar \
                -httpPort=8080 \
                -extractDirectory=.extract/Palisade \
                -Dpalisade.rest.config.path=$PWD/example/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=palisade \
                -Dpalisade.properties.app.title=rest-palisade-service