#!/usr/bin/env bash

# Get script dir
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

# Initialise bucket and upload file
"$DIR/../../scripts/initDockerS3.sh" "$DIR/../../resources/exampleObj_file1.txt" Palisade_example_bucket

# Set Hadoop extra configuration
export HADOOP_CONF_PATH="$DIR/../../resources/hadoop_minio_s3.xml"

# Run Palisade example
java -cp example/single-jvm-example/single-jvm-example-runner/target/single-jvm-example-runner-*-shaded.jar uk.gov.gchq.palisade.example.SingleJvmExample "$DIR/../../resources/exampleObj_file1.txt"

# Terminate the docker container
"$DIR/../../scripts/stopDockerS3.sh"