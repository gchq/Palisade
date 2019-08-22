#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/setScriptPath.sh"

export PALISADE_REST_CONFIG_PATH="/home/ec2-user/example/example-model/src/main/resources/configRest.json"

# Runs the demo example, start to finish, all users, all purposes 
# java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.runner.RestExample $1 | /home/ec2-user/example/deployment/bash-scripts/formatOutput.sh

# Runs the example and allows user to pass in a name, file and purpose
java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.client.ExampleSimpleClient $1 $2 $3 | /home/ec2-user/example/deployment/bash-scripts/formatOutput.sh
