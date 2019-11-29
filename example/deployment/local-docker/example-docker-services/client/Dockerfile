FROM openjdk:8-jre-alpine

RUN apk add --no-cache curl
COPY docker/bin/example-model-*-shaded.jar /example-model/
ADD docker/bin/configRest.json /example-model/

ENV PALISADE_REST_CONFIG_PATH="/example-model/configRest.json"

ENTRYPOINT ["java"]

CMD ["-cp", "/example-model/*", "uk.gov.gchq.palisade.example.runner.RestExample" , "/data" ]
