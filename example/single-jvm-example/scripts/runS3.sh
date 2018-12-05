#!/usr/bin/env bash
#launch s3 docker container here, copy the file in
#set the hadoop extra configuration properties file
#tell the jvmexample to run from s3, not the local example file
java -cp example/single-jvm-example/single-jvm-example-runner/target/single-jvm-example-runner-*-shaded.jar uk.gov.gchq.palisade.example.SingleJvmExample
#terminate the docker container