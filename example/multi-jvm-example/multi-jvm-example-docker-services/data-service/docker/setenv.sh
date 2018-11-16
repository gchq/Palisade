#!/usr/bin/env bash
export JAVA_OPTS="-Dpalisade.rest.basePath=data-service \
                  -Dpalisade.properties.app.title=rest-data-service \
                  -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/data/WEB-INF/configRest.json"