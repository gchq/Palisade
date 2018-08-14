FROM alpine/git as clone

WORKDIR /app
RUN git clone https://github.com/gchq/Palisade.git
WORKDIR /app/Palisade
RUN git checkout -b gh-10-containerise-multi-jvm-example

FROM maven:3.5-jdk-8-alpine as build

WORKDIR /app
COPY --from=clone /app/Palisade /app
RUN mvn clean install -Pquick;

ENTRYPOINT cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-data-service/target/multi-jvm-example-rest-data-service-0.0.1-SNAPSHOT/* /data-vol/bin/rest-data-service/; \
           cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-palisade-service/target/multi-jvm-example-rest-palisade-service-0.0.1-SNAPSHOT/* /palisade-vol/bin/rest-palisade-service/; \
           cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-policy-service/target/multi-jvm-example-rest-policy-service-0.0.1-SNAPSHOT/* /policy-vol/bin/rest-policy-service/; \
           cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-resource-service/target/multi-jvm-example-rest-resource-service-0.0.1-SNAPSHOT/* /resource-vol/bin/rest-resource-service/; \
           cp -r /app/example/multi-jvm-example/multi-jvm-example-rest-user-service/target/multi-jvm-example-rest-user-service-0.0.1-SNAPSHOT/* /user-vol/bin/rest-user-service/


