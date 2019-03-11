FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
COPY docker/bin/example-rest-redirector-*-shaded.jar /example-rest-redirector/
ADD docker/bin/configRest.json /example-rest-redirector/

ENTRYPOINT ["java"]
CMD ["-cp", "/example-rest-redirector/*", "-Dpalisade.rest.basePath=http://0.0.0.0:8080/data/v1", "-Dpalisade.rest.config.path=/example-rest-redirector/configRest.json", "uk.gov.gchq.palisade.redirect.Launcher"]
