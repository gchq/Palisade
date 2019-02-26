#!/usr/bin/env bash

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
java -jar $RESTEXAMPLE/example-rest-palisade-service/target/example-rest-palisade-service-*-executable.jar \
                -httpPort=8080 \
                -extractDirectory=.extract/Palisade \
                -Dpalisade.rest.config.path=$EXAMPLESERVICES/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=palisade \
                -Dpalisade.properties.app.title=rest-palisade-service