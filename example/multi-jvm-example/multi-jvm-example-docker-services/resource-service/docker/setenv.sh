#!/usr/bin/env bash
export JAVA_OPTS="-Dpalisade.rest.basePath=resource \
                  -Dpalisade.properties.app.title=rest-resource-service \
                  -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/resource/WEB-INF/configRest.json"
