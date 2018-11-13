#!/usr/bin/env bash

mvn clean install -Pmulti-jvm-example-rest-palisade-service -pl :multi-jvm-example-rest-palisade-service -Dstandalone-path=palisade -Dstandalone-port=8080 $@
