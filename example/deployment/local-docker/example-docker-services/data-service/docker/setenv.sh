#!/usr/bin/env bash

export PALISADE_REST_CONFIG_PATH="/usr/local/tomcat/webapps/data/WEB-INF/configRest.json"

export JAVA_OPTS="-Dpalisade.rest.basePath=data \
                  -Dpalisade.properties.app.title=rest-data-service"
