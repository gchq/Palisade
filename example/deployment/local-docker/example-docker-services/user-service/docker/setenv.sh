#!/usr/bin/env bash

export PALISADE_REST_CONFIG_PATH="/usr/local/tomcat/webapps/user/WEB-INF/configRest.json"

export JAVA_OPTS="-Dpalisade.rest.basePath=user \
                  -Dpalisade.properties.app.title=rest-user-service
