FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
COPY docker/bin/example-docker-runner-*-shaded.jar /example-docker-runner/
ADD docker/bin/configRest.json /example-docker-runner/

ENV PALISADE_REST_CONFIG_PATH="/example-docker-runner/configRest.json"

ENTRYPOINT ["java"]
CMD ["-cp", "/example-docker-runner/*", "uk.gov.gchq.palisade.example.RestDockerExample"]
