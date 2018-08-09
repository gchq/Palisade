#!/usr/bin/env bash
export JAVA_OPTS="-Dpalisade.rest.basePath=policy \
                  -Dpalisade.properties.app.title=rest-policy-service \
                  -Dpalisade.rest.policy.service.config.path=/usr/local/tomcat/webapps/policy/WEB-INF/policyConfig.json"
