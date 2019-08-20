From tomcat:8-jre8-alpine

RUN apk add --no-cache curl
ADD docker/bin/rest-user-service /usr/local/tomcat/webapps/user/
ADD docker/setenv.sh /usr/local/tomcat/bin/
ADD docker/bin/configRest.json /usr/local/tomcat/webapps/user/WEB-INF/

