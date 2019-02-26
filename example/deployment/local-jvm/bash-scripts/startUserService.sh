#!/usr/bin/env bash

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
java -jar $RESTEXAMPLE/example-rest-user-service/target/example-rest-user-service-*-executable.jar \
                -httpPort=8083 \
                -extractDirectory=.extract/User \
                -Dpalisade.rest.config.path=$EXAMPLESERVICES/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=user \
                -Dpalisade.properties.app.title=rest-user-service
