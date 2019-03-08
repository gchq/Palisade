#!/usr/bin/env bash

. ./example/deployment/bash-scripts/setScriptPath.sh
java -jar $EXAMPLESERVICES/example-rest-policy-service/target/example-rest-policy-service-*-executable.jar \
                -httpPort=8081 \
                -extractDirectory=.extract/Policy \
                -Dpalisade.rest.config.path=$PWD/example/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=policy \
                -Dpalisade.properties.app.title=rest-policy-service
