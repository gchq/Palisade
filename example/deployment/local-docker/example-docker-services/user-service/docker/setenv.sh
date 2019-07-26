#!/usr/bin/env bash

JAVA_OPTS="-Dpalisade.rest.basePath=user \
                  -Dpalisade.properties.app.title=rest-user-service \
                  -DPALISADE_REST_CONFIG_PATH=/usr/local/tomcat/webapps/user/WEB-INF/configRest.json"
