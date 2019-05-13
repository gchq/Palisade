#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/../../bash-scripts/setScriptPath.sh"

"${LOCALJVMBASHSCRIPTS}/stopAllServices.sh" > /dev/null 2>&1

# deploy etcd
"${LOCALJVMBASHSCRIPTS}/startETCD.sh"
if [[ $? -ne 0 ]]; then
    exit;
fi

#start services
"${GENERICSCRIPTS}/waitForHost.sh" localhost:2379/health "${LOCALJVMBASHSCRIPTS}/startConfigService.sh" &
"${GENERICSCRIPTS}/waitForHost.sh" http://localhost:8085/config/v1/status "${LOCALJVMBASHSCRIPTS}/configureServices.sh"
"${LOCALJVMBASHSCRIPTS}/startPolicyService.sh" &
"${LOCALJVMBASHSCRIPTS}/startResourceService.sh" &
"${LOCALJVMBASHSCRIPTS}/startUserService.sh" &
"${LOCALJVMBASHSCRIPTS}/startPalisadeService.sh" &
"${LOCALJVMBASHSCRIPTS}/startDataService.sh" &
"${GENERICSCRIPTS}/waitForHost.sh" http://localhost:8081/policy/v1/status "${GENERICSCRIPTS}/waitForHost.sh" http://localhost:8082/resource/v1/status \
     "${GENERICSCRIPTS}/waitForHost.sh" http://localhost:8083/user/v1/status "${LOCALJVMBASHSCRIPTS}/configureExamples.sh"
