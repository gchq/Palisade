#!/usr/bin/env bash
export JAVA_OPTS="-Dpalisade.rest.basePath=redirector \
                  -Dpalisade.properties.app.title=rest-redirector \
                  -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/policy/WEB-INF/configRest.json"
