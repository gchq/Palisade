FROM openjdk:8-alpine

COPY target/multi-jvm-example-docker-runner-*-SNAPSHOT-shaded.jar /multi-jvm-example-docker-runner/
ENTRYPOINT [ "java" ]
CMD ["-cp", "/multi-jvm-example-docker-runner/*", "uk.gov.gchq.palisade.example.MultiJVMDockerExample" ]

