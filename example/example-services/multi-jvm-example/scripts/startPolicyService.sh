#!/usr/bin/env bash

java -jar example/example-services/multi-jvm-example/multi-jvm-example-rest-policy-service/target/multi-jvm-example-rest-policy-service-*-executable.jar \
                -httpPort=8081 \
                -extractDirectory=.extract/Policy \
                -Dpalisade.rest.config.path=example/example-services/example-model/src/main/resources/configRest.json \
                -Dpalisade.rest.basePath=policy \
                -Dpalisade.properties.app.title=rest-policy-service
