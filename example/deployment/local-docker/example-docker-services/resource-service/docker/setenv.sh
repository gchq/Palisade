#!/usr/bin/env bash

export PALISADE_REST_CONFIG_PATH="/usr/local/tomcat/webapps/resource/WEB-INF/configRest.json"

export JAVA_OPTS="-Dpalisade.rest.basePath=resource \
                  -Dpalisade.properties.app.title=rest-resource-service
