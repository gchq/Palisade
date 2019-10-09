#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/setScriptPath.sh"

# Path for compiled JAR
TARGET_DIR="${EXAMPLE}/example-model/target"

FILE_PRESENT=0

if [ -d "$TARGET_DIR" ];
then
    MODEL_JAR=$(find "${EXAMPLE}/example-model/target" -type f -iname "example-model-*-shaded.jar")
    if [ ! -z "$MODEL_JAR" ];
    then
        FILE_PRESENT=1
    fi
fi

if [ "$FILE_PRESENT" -eq 0 ];then
    echo "Can't find example-model-<version>-SNAPSHOT.jar in ${TARGET_DIR}. Have you run \"mvn install -P example\" ?"
    exit 1;
fi

# Run the bulk resource test
PALISADE_REST_CONFIG_PATH=example/example-model/src/main/resources/configRest.json java -cp $MODEL_JAR uk.gov.gchq.palisade.example.runner.BulkTestExample "${EXAMPLE}/resources/data" $@
