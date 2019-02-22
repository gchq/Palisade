#!/usr/bin/env bash

java -jar example/example-services/multi-jvm-example/multi-jvm-example-rest-data-service/target/multi-jvm-example-rest-data-service-*-executable.jar \
                -httpPort=8084 \
                -extractDirectory=.extract/Data \
                -Dpalisade.rest.config.path=example/example-services/example-model/src/main/resources/configRest.json \
                -Dpalisade.properties.app.title=rest-data-service \
                -Dpalisade.rest.basePath=data
