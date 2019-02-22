From tomcat:8-jre11 as data-service

ADD bin/rest-data-service/ /usr/local/tomcat/webapps/data
ADD conf/dataConfig.json /usr/local/tomcat/webapps/data/WEB-INF/
ENV JAVA_OPTS="-Dpalisade.rest.basePath=data-service -Dpalisade.properties.app.title=rest-data-service -Dpalisade.rest.config.path=/usr/local/tomcat/webapps/data/WEB-INF/configRest.json"
