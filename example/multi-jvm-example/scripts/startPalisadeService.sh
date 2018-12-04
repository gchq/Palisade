#!/usr/bin/env bash

java -jar example/multi-jvm-example/multi-jvm-example-rest-palisade-service/target/multi-jvm-example-rest-palisade-service-*-executable.jar -httpPort=8080 -extractDirectory=.extract/Palisade -Dpalisade.rest.config.path=example/example-model/src/main/resources/configRest.json

