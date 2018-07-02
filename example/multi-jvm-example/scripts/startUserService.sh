#!/usr/bin/env bash

mvn clean install -Pquick -Pmulti-jvm-example-rest-user-service -pl :multi-jvm-example-rest-user-service -Dstandalone-path=user -Dstandalone-port=8083 $@
