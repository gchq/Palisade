#!/usr/bin/env bash

java -jar example/example-services/multi-jvm-example/multi-jvm-example-rest-resource-service/target/multi-jvm-example-rest-resource-service-*-executable.jar \
                -httpPort=8082 \
                -extractDirectory=.extract/Resource \
                -Dpalisade.rest.config.path=example/example-services/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=resource \
                -Dpalisade.properties.app.title=rest-resource-service
