#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

java -cp "$EXAMPLE"/example-model/target/example-model-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.config.ExampleConfigurator "$EXAMPLE/resources/employee_file0.avro"
#uncomment the line below to run locally against a local pseudo distributed hadoop cluster
#java -cp "$EXAMPLE"/example-model/target/example-model-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.config.ExampleConfigurator "hdfs://localhost/example_data/employee_file0.avro"
