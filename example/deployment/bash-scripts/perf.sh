#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/setScriptPath.sh"

# Path for compiled JAR
TARGET_DIR="${EXAMPLE}/performance/target"

FILE_PRESENT=0

if [ -d "$TARGET_DIR" ];
then
    JAR_FILE=$(find "$TARGET_DIR" -type f -iname "performance-*.jar")
    MODEL_JAR=$(find "${EXAMPLE}/example-model/target" -type f -iname "example-model-*-shaded.jar")
    if [ ! -z "$JAR_FILE" ];
    then
        FILE_PRESENT=1
    fi
fi

if [ "$FILE_PRESENT" -eq 0 ];then
    echo "Can't find performance-<version>-SNAPSHOT.jar in ${TARGET_DIR}. Have you run \"mvn install -P example\" ?"
    exit 1;
fi

# Run the performance tool
PALISADE_REST_CONFIG_PATH=example/example-model/src/main/resources/configRest.json java -cp $JAR_FILE:$MODEL_JAR uk.gov.gchq.palisade.example.perf.Perf $@
