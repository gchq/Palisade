#!/usr/bin/env bash

export DIR=example/multi-jvm-example/scripts

${DIR}/stopAllServices.sh > /dev/null 2>&1

# deploy etcd
${DIR}/startETCD.sh &
${DIR}/waitForHost.sh localhost:2379/health ${DIR}/startConfigService.sh &
${DIR}/waitForHost.sh http://localhost:8085/config/v1/status ${DIR}/configureServices.sh
${DIR}/startPolicyService.sh &
${DIR}/startResourceService.sh &
${DIR}/startUserService.sh &
${DIR}/startPalisadeService.sh &
${DIR}/startDataService.sh &
${DIR}/waitForHost.sh http://localhost:8081/policy/v1/status ${DIR}/waitForHost.sh http://localhost:8082/resource/v1/status ${DIR}/waitForHost.sh http://localhost:8083/user/v1/status ${DIR}/configureExamples.sh
