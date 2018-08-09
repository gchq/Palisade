From tomcat:8.0-jre8-alpine

ADD bin/rest-policy-service /usr/local/tomcat/webapps/policy/
ADD conf/policyConfig.json /usr/local/tomcat/webapps/policy/WEB-INF/
ENV JAVA_OPTS="-Dpalisade.rest.basePath=policy \
               -Dpalisade.properties.app.title=rest-policy-service \
               -Dpalisade.rest.policy.service.config.path=/usr/local/tomcat/webapps/policy/WEB-INF/policyConfig.json"
