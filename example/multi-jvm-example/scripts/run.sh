#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

java -cp example/multi-jvm-example/multi-jvm-example-runner/target/multi-jvm-example-runner-*-shaded.jar uk.gov.gchq.palisade.example.MultiJvmExample "$DIR/../../resources/exampleObj_file1.txt"
