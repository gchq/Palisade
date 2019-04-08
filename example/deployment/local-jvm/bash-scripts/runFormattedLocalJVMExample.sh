#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

java -cp "$EXAMPLE"/deployment/local-jvm/example-runner/target/example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.RestExample \
    "$EXAMPLE/resources/exampleEmployee_file0.avro" | sed $'s/Alice \[/\\\n\\\nAlice \[/g'| sed $'s/Bob \[/\\\n\\\nBob \[/g' | \
    sed $'s/Eve \[/\\\n\\\nEve \[/g' | sed $'s/,name=/\\\n\\\nname=/g'|sed $'s/,dateOfBirth=/\\\ndateOfBirth=/g'|sed $'s/,contactNumbers=/\\\ncontactNumbers=/g'| \
    sed $'s/,emergencyContacts=/\\\nemergencyContacts=/g'|sed $'s/,address=/\\\naddress=/g'|sed $'s/,bankDetails=/\\\nbankDetails=/g'| \
    sed $'s/,taxCode=/\\\ntaxCode=/g'|sed $'s/,nationality=/\\\nnationality=/g'
