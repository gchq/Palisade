#!/usr/bin/env bash

mvn install -Pmulti-jvm-example-rest-palisade-service -pl :multi-jvm-example-rest-palisade-service -Dstandalone-path=palisade -Dstandalone-port=8080
mvn install -Pmulti-jvm-example-rest-policy-service -pl :multi-jvm-example-rest-policy-service -Dstandalone-path=policy -Dstandalone-port=8081
mvn install -Pmulti-jvm-example-rest-resource-service -pl :multi-jvm-example-rest-resource-service -Dstandalone-path=resource -Dstandalone-port=8082
mvn install -Pmulti-jvm-example-rest-user-service -pl :multi-jvm-example-rest-user-service -Dstandalone-path=user -Dstandalone-port=8083
mvn install -Pmulti-jvm-example-rest-data-service -pl :multi-jvm-example-rest-data-service -Dstandalone-path=data -Dstandalone-port=8084
mvn install -Pmulti-jvm-example-rest-config-service -pl :multi-jvm-example-rest-config-service -Dstandalone-path=config -Dstandalone-port=8085
