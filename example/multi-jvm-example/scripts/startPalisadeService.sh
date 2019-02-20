#!/usr/bin/env bash

java -jar example/multi-jvm-example/multi-jvm-example-rest-palisade-service/target/multi-jvm-example-rest-palisade-service-*-executable.jar \
                -httpPort=8080 \
                -extractDirectory=.extract/Palisade \
                -Dpalisade.rest.config.path=example/multi-jvm-example/multi-jvm-example-docker-services/configRest.json \
                -Dpalisade.rest.basePath=palisade \
                -Dpalisade.properties.app.title=rest-palisade-service