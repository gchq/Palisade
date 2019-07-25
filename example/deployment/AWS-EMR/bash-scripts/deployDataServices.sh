#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster from the hadoop users home directory
#
# This script will start a data service on each of the slave/core Yarn nodes - listening on port 8084

if [ $# -gt 0 ]; then
    # need to pass in the location of your .pem file
    key=$1

    if [ ${key: -4} == ".pem" ]; then
        # identify all the yarn nodes in the cluster
        declare nodes=`yarn node -list 2> /dev/null | grep internal | cut -c 4- | tr '.' '\t' | cut -f1 | tr '-' '.'`
        sed -i "s/localhost/${HOSTNAME}/g" /home/hadoop/deploy_example/resources/configRest.json
        for node in $nodes
         do
           ssh -f -i $key -o StrictHostKeyChecking=no hadoop@$node "sudo pkill -9 java || echo Killed; \
                                                                    mkdir -p /home/hadoop/deploy_example/resources; \
                                                                    mkdir -p /home/hadoop/jars; \
                                                                    mkdir -p /home/hadoop/example_data; \
                                                                    mkdir -p /home/hadoop/example_logs"
           scp -i $key -r -q /home/hadoop/deploy_example/resources/configRest.json hadoop@$node:/home/hadoop/deploy_example/resources
           scp -i $key -r -q /home/hadoop/jars/example-rest-data-service-*-executable.jar hadoop@$node:/home/hadoop/jars/
           ssh -f -i $key -o StrictHostKeyChecking=no hadoop@$node "PALISADE_REST_CONFIG_PATH=/home/hadoop/deploy_example/resources/configRest.json \
                                                                    java -jar /home/hadoop/jars/example-rest-data-service-*-executable.jar \
                                                                    -httpPort=8084 \
                                                                    -extractDirectory=.extract/Data \
                                                                    -Dpalisade.properties.app.title=rest-data-service > /home/hadoop/example_logs/deployDataService.log 2>&1 "
         done
    else
        echo "The first argument should be to a .pem file."
    fi
else
    echo "Your command line contains no arguments, you need to supply the .pem file associated with your emr cluster."
fi
