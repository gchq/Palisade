#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

java -cp example/single-jvm-example/single-jvm-example-runner/target/single-jvm-example-runner-*-shaded.jar uk.gov.gchq.palisade.example.SingleJvmExample "$DIR/../../resources/exampleObj_file1.txt"
