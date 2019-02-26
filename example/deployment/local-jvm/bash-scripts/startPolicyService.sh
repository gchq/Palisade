#!/usr/bin/env bash

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
java -jar $RESTEXAMPLE/example-rest-policy-service/target/example-rest-policy-service-*-executable.jar \
                -httpPort=8081 \
                -extractDirectory=.extract/Policy \
                -Dpalisade.rest.config.path=$EXAMPLESERVICES/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=policy \
                -Dpalisade.properties.app.title=rest-policy-service
