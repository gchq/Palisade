#!/usr/bin/env bash

mvn clean install -Pquick -Pmulti-jvm-example-rest-data-service -pl :multi-jvm-example-rest-data-service -Dstandalone-path=data -Dstandalone-port=8084 $@
