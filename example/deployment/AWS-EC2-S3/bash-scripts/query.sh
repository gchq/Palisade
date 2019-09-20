#!/usr/bin/env bash

export PALISADE_REST_CONFIG_PATH="/home/ec2-user/example/example-model/src/main/resources/configRest.json"

if [[ $# -eq 1 ]]; then
    java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.client.ExampleSimpleClient $1 s3a://palisade-ec2-demo/data "" | /home/ec2-user/example/deployment/bash-scripts/formatOutput.sh
else
    java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.client.ExampleSimpleClient $1 s3a://palisade-ec2-demo/data $2 | /home/ec2-user/example/deployment/bash-scripts/formatOutput.sh
fi

# Runs the demo example, start to finish, all users, all purposes
# java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.runner.RestExample $1 | /home/ec2-user/example/deployment/bash-scripts/formatOutput.sh

# Runs the example and allows user to pass in a name, file and purpose
# java -cp /home/ec2-user/example/example-model/target/example-model-*-shaded.jar uk.gov.gchq.palisade.example.client.ExampleSimpleClient $1 $2 $3 | /home/ec2-user/example/deployment/bash-scripts/formatOutput.sh