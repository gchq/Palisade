#!/usr/bin/env bash

mvn clean install -Pmulti-jvm-example-rest-policy-service -pl :multi-jvm-example-rest-policy-service -Dstandalone-path=policy -Dstandalone-port=8081 $@
