#!/usr/bin/env bash

mvn clean install -Pmulti-jvm-example-rest-resource-service -pl :multi-jvm-example-rest-resource-service -Dstandalone-path=resource -Dstandalone-port=8082 $@
