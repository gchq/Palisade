#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will deploy an instance of the config service on the master node - listening on port 8085

# identify all the yarn nodes in the cluster
declare nodes=`yarn node -list 2> /dev/null | grep internal | cut -c 4- | tr '.' '\t' | cut -f1 | tr '-' '.'`

# create the -initial-cluster string
for node in $nodes
do
   if [ -z "${etcd_connection_details}" ]; then
     etcd_connection_details="\\\"http://$node:2379\\\""
   else
     etcd_connection_details="${etcd_connection_details},\\\"http://$node:2379\\\""
   fi
done

sed -i "s|<insert_etcd_connection_details>|${etcd_connection_details}|g" /home/hadoop/deploy_example/resources/bootstrapConfig.json

java -jar /home/hadoop/jars/example-rest-config-service-*-executable.jar \
    -httpPort=8085 \
    -extractDirectory=.extract/Config \
    -Dpalisade.rest.bootstrap.path=/home/hadoop/deploy_example/resources/bootstrapConfig.json \
    -Dpalisade.rest.basePath=config \
    -Dpalisade.properties.app.title=rest-config-service
