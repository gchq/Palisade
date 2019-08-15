#!/usr/bin/env bash

mvn clean install -Pexample-rest-palisade-service -pl :example-rest-palisade-service -Dstandalone-path=palisade -Dstandalone-port=8080
mvn clean install -Pexample-rest-policy-service -pl :example-rest-policy-service -Dstandalone-path=policy -Dstandalone-port=8081
mvn clean install -Pexample-rest-resource-service -pl :example-rest-resource-service -Dstandalone-path=resource -Dstandalone-port=8082
mvn clean install -Pexample-rest-user-service -pl :example-rest-user-service -Dstandalone-path=user -Dstandalone-port=8083
mvn clean install -Pexample-rest-data-service -pl :example-rest-data-service -Dstandalone-path=data -Dstandalone-port=8084
mvn clean install -Pexample-rest-config-service -pl :example-rest-config-service -Dstandalone-path=config -Dstandalone-port=8085

wait
