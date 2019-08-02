#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="/home/ec2-user/example/example-model/src/main/resources/configRest.json"

java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.runner.RestExample $1 | /home/ec2-user/example/deployment/bash-scripts/formatOutput.sh