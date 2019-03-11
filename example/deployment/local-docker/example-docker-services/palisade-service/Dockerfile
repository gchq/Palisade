From tomcat:8-jre8-alpine

RUN apk add --no-cache curl
ADD docker/bin/rest-palisade-service /usr/local/tomcat/webapps/palisade/
ADD docker/setenv.sh /usr/local/tomcat/bin/
ADD docker/bin/configRest.json /usr/local/tomcat/webapps/palisade/WEB-INF/

