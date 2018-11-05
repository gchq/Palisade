From tomcat:8.0-jre8-alpine

ADD bin/rest-policy-service /usr/local/tomcat/webapps/policy/
ADD bin/configRest.json /usr/local/tomcat/webapps/policy/WEB-INF/
ENV JAVA_OPTS="-Dpalisade.rest.basePath=policy \
               -Dpalisade.properties.app.title=rest-policy-service \
               -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/policy/WEB-INF/configRest.json
