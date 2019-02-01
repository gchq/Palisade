FROM openjdk:8-alpine

RUN apk add --no-cache curl
ADD docker/bin/example-model-*-shaded.jar /tmp/
ADD waitForHost.sh /tmp/
