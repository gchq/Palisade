#!/usr/bin/env bash

mvn clean install -Pquick -Pmulti-jvm-example-rest-config-service -pl :multi-jvm-example-rest-config-service -Dstandalone-path=palisade -Dstandalone-port=8085 $@
