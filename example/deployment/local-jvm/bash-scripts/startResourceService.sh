#!/usr/bin/env bash

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
java -jar $RESTEXAMPLE/example-rest-resource-service/target/example-rest-resource-service-*-executable.jar \
                -httpPort=8082 \
                -extractDirectory=.extract/Resource \
                -Dpalisade.rest.config.path=$EXAMPLESERVICES/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=resource \
                -Dpalisade.properties.app.title=rest-resource-service
