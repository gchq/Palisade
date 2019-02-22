#!/usr/bin/env bash

java -jar example/example-services/multi-jvm-example/multi-jvm-example-rest-palisade-service/target/multi-jvm-example-rest-palisade-service-*-executable.jar \
                -httpPort=8080 \
                -extractDirectory=.extract/Palisade \
                -Dpalisade.rest.config.path=example/example-services/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=palisade \
                -Dpalisade.properties.app.title=rest-palisade-service