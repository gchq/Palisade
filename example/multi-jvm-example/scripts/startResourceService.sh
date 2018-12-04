#!/usr/bin/env bash

java -jar example/multi-jvm-example/multi-jvm-example-rest-resource-service/target/multi-jvm-example-rest-resource-service-*-executable.jar -httpPort=8082 -extractDirectory=.extract/Resource -Dpalisade.rest.config.path=example/example-model/src/main/resources/configRest.json
