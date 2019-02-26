#!/usr/bin/env bash

. ./example/deployment/local-jvm/bash-scripts/setScriptPath.sh
java -jar $MULTIJVMEXAMPLE/multi-jvm-example-rest-data-service/target/multi-jvm-example-rest-data-service-*-executable.jar \
                -httpPort=8084 \
                -extractDirectory=.extract/Data \
                -Dpalisade.rest.config.path=$EXAMPLESERVICES/example-model/src/main/resources/configRest.json \
                -Dpalisade.properties.app.title=rest-data-service \
                -Dpalisade.rest.basePath=data
