#!/usr/bin/env bash

mvn clean install -Pmulti-jvm-example-rest-config-service -pl :multi-jvm-example-rest-config-service -Dstandalone-path=config -Dstandalone-port=8085 $@
