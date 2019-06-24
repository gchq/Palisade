FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
COPY docker/bin/example-rest-redirector-*-shaded.jar /example-rest-redirector/
ADD docker/bin/configRest.json /example-rest-redirector/

ENV PALISADE_REST_CONFIG_PATH="/example-rest-redirector/configRest.json"

ENTRYPOINT ["java"]
CMD ["-cp", "/example-rest-redirector/*", "-Dpalisade.rest.basePath=http://0.0.0.0:8080/data/v1", "uk.gov.gchq.palisade.redirect.service.Launcher"]
