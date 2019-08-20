#!/usr/bin/env bash
#populate the config service with the addresses of all the other services, requires the config service to be running

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

java -cp "$EXAMPLE"/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.config.LocalServices
