FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
COPY docker/bin/example-docker-runner-*-shaded.jar /example-docker-runner/
ADD docker/bin/configRest.json /example-docker-runner/

ARG PALISADE_REST_CONFIG_PATH="/example-docker-runner/configRest.json"
ENV PALISADE_REST_CONFIG_PATH=$PALISADE_REST_CONFIG_PATH

ENTRYPOINT ["java"]
CMD ["-cp", "/example-docker-runner/*", "uk.gov.gchq.palisade.example.RestDockerExample"]
