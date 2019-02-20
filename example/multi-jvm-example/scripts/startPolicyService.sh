#!/usr/bin/env bash

java -jar example/multi-jvm-example/multi-jvm-example-rest-policy-service/target/multi-jvm-example-rest-policy-service-*-executable.jar \
                -httpPort=8081 \
                -extractDirectory=.extract/Policy \
                -Dpalisade.rest.config.path=example/multi-jvm-example/multi-jvm-example-docker-services/configRest.json \
                -Dpalisade.rest.basePath=policy \
                -Dpalisade.properties.app.title=rest-policy-service
