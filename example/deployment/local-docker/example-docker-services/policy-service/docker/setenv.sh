#!/usr/bin/env bash

export PALISADE_REST_CONFIG_PATH="/usr/local/tomcat/webapps/policy/WEB-INF/configRest.json"

export JAVA_OPTS="-Dpalisade.rest.basePath=policy \
                  -Dpalisade.properties.app.title=rest-policy-service"
