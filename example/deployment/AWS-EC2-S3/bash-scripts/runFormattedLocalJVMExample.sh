#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="configRest.json"

java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.runner.RestExample "s3://developer6959palisadeec2test.s3-eu-west-1.amazonaws.com/employee_file0.avro"

#"s3:/developer6959palisadeec2test/employee_file0.avro"

    #| $EXAMPLE/deployment/bash-scripts/formatOutput.sh
