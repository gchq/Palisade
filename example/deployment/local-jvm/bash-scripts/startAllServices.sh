#!/usr/bin/env bash

export LOCAL_JVM_SCRIPTS=example/deployment/local-jvm/bash-scripts
export GENERIC_SCRIPTS=example/deployment/bash-scripts

${LOCAL_JVM_SCRIPTS}/stopAllServices.sh > /dev/null 2>&1

# deploy etcd
${LOCAL_JVM_SCRIPTS}/startETCD.sh
if [[ $? -ne 0 ]]; then
    exit;
fi

#start services
${GENERIC_SCRIPTS}/waitForHost.sh localhost:2379/health ${LOCAL_JVM_SCRIPTS}/startConfigService.sh &
${GENERIC_SCRIPTS}/waitForHost.sh http://localhost:8085/config/v1/status ${LOCAL_JVM_SCRIPTS}/configureServices.sh
${LOCAL_JVM_SCRIPTS}/startPolicyService.sh &
${LOCAL_JVM_SCRIPTS}/startResourceService.sh &
${LOCAL_JVM_SCRIPTS}/startUserService.sh &
${LOCAL_JVM_SCRIPTS}/startPalisadeService.sh &
${LOCAL_JVM_SCRIPTS}/startDataService.sh &
${GENERIC_SCRIPTS}/waitForHost.sh http://localhost:8081/policy/v1/status ${GENERIC_SCRIPTS}/waitForHost.sh http://localhost:8082/resource/v1/status ${GENERIC_SCRIPTS}/waitForHost.sh http://localhost:8083/user/v1/status ${LOCAL_JVM_SCRIPTS}/configureExamples.sh
