#!/usr/bin/env bash

java -jar example/multi-jvm-example/multi-jvm-example-rest-user-service/target/multi-jvm-example-rest-user-service-*-executable.jar \
                -httpPort=8083 \
                -extractDirectory=.extract/User \
                -Dpalisade.rest.config.path=example/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=user \
                -Dpalisade.properties.app.title=rest-user-service
