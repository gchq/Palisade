#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="configRest.json"

java -cp "$EXAMPLE"/deployment/local-jvm/example-runner/target/example-runner-*-shaded.jar uk.gov.gchq.palisade.example.RestExample "$EXAMPLE/resources/employee_file0.avro"
