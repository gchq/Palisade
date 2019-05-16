#!/usr/bin/env bash

export PALISADE_REST_CONFIG_PATH="/usr/local/tomcat/webapps/palisade/WEB-INF/configRest.json"

export JAVA_OPTS="-Dpalisade.rest.basePath=palisade \
                  -Dpalisade.properties.app.title=rest-palisade-service
