#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
OUT_PATH="/tmp/palisade-mapreduce-example/output"

# Start docker services for S3

"$DIR/../../scripts/initDockerS3.sh" "$DIR/../../resources/exampleObj_file1.txt" palisade-example-bucket

# Ensure ExampleConfigurator loads the correct Hadoop configuration file that defines the S3 file system
export HADOOP_CONF_PATH="$DIR/../../resources/hadoop_minio_s3.xml"

# Run S3 example
"$DIR/run.sh" "$OUT_PATH" "s3a://palisade-example-bucket/exampleObj_file1.txt"

# Stop the docker services
"$DIR/../../scripts/stopDockerS3.sh"
