#!/usr/bin/env bash
set -e

# Check that the required network is present
NETWORKS=$(docker network ls -f 'name=palisade_example' --format '{{.Name}}')
if [[ $(grep -c palisade_example <(echo $NETWORKS)) -lt 1 ]]; then
    echo "Docker network \"palisade_example\" is not present. Do you need to run the dockerComposeUp.sh script?"
    exit 1
fi

docker build -t multi-jvm-example-docker-runner ./example/multi-jvm-example/multi-jvm-example-docker-runner/
docker run --network=palisade_example_palisade_network --rm multi-jvm-example-docker-runner
