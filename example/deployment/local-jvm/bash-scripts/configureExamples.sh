#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

java -cp "$EXAMPLE"/example-model/target/example-model-*-shaded.jar -Dpalisade.rest.config.path=configRest.json -Dpalisade.serialiser.json.modules=uk.gov.gchq.palisade.example.common.ExampleSerialiserModules uk.gov.gchq.palisade.example.config.ExampleConfigurator  "$EXAMPLE/resources/employee_file0.avro"
