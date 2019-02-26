#!/usr/bin/env bash

. ./example/deployment/multi-use/bash-scripts/setScriptPath.sh
java -jar $MULTIJVMEXAMPLE/multi-jvm-example-rest-resource-service/target/multi-jvm-example-rest-resource-service-*-executable.jar \
                -httpPort=8082 \
                -extractDirectory=.extract/Resource \
                -Dpalisade.rest.config.path=$EXAMPLESERVICES/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=resource \
                -Dpalisade.properties.app.title=rest-resource-service
