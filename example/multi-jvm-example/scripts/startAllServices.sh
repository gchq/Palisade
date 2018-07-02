#!/usr/bin/env bash

example/multi-jvm-example/scripts/stopAllServices.sh > /dev/null 2>&1

example/multi-jvm-example/scripts/startDataService.sh &
example/multi-jvm-example/scripts/startPalisadeService.sh &
example/multi-jvm-example/scripts/startPolicyService.sh &
example/multi-jvm-example/scripts/startResourceService.sh &
example/multi-jvm-example/scripts/startUserService.sh &
