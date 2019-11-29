#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/setScriptPath.sh"

# Check if necessary compiled JAR is present
TARGET_DIR="${EXAMPLE}/hr-data-generator/target"

FILE_PRESENT=0

if [ -d "$TARGET_DIR" ];
then
    JAR_FILE=$(find "$TARGET_DIR" -type f -iname "hr-data-generator-*-shaded.jar")
    if [ ! -z "$JAR_FILE" ];
    then
        FILE_PRESENT=1
    fi
fi

if [ "$FILE_PRESENT" -eq 0 ];then
    echo "Can't find hr-data-generator-<version>-shaded.jar in ${TARGET_DIR}. Have you run \"mvn install -P example\" ?"
    exit 1;
fi

# Run the generator
java -cp $JAR_FILE uk.gov.gchq.palisade.example.hrdatagenerator.CreateData $@
