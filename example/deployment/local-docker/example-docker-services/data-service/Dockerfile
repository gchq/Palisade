From tomcat:8-jre8-alpine

RUN apk add --no-cache curl
ADD docker/bin/rest-data-service/ /usr/local/tomcat/webapps/data
ADD docker/setenv.sh /usr/local/tomcat/bin/
ADD docker/bin/configRest.json /usr/local/tomcat/webapps/data/WEB-INF/

