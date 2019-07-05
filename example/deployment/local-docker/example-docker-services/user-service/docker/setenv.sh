#!/usr/bin/env bash

PALISADE_REST_CONFIG_PATH="/usr/local/tomcat/webapps/user/WEB-INF/configRest.json"

JAVA_OPTS="-Dpalisade.rest.basePath=user \
                  -Dpalisade.properties.app.title=rest-user-service"
