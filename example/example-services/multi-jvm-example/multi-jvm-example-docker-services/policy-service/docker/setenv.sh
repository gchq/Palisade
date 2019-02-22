#!/usr/bin/env bash
export JAVA_OPTS="-Dpalisade.rest.basePath=policy \
                  -Dpalisade.properties.app.title=rest-policy-service \
                  -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/policy/WEB-INF/configRest.json"
