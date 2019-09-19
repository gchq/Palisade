#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="configRest.json"

java -cp "$EXAMPLE"/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.runner.RestExample "$EXAMPLE/resources/data/"
