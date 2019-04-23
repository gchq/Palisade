#!/bin/bash

# This script assumes it is running on the master node on an AWS EMR cluster
# This script will configure the Palisade Config service (which needs to be already running) so that it knows how the various Palisade services are distributed over the cluster

private_dns=`hostname -f`
echo "Hostname: "$private_dns

# identify all the yarn nodes in the cluster
declare nodes=`yarn node -list 2> /dev/null | grep internal | cut -c 4- | tr '.' '\t' | cut -f1 | tr '-' '.'`

# create the cluster address strings
for node in $nodes
do
   if [ -z "${etcd_connection_details}" ]; then
     etcd_connection_details="http://$node:2379"
     data_connection_details="http://$node:8084/data"
   else
     etcd_connection_details=${etcd_connection_details},"http://$node:2379"
     data_connection_details=${data_connection_details},"http://$node:8084/data"
   fi
done

echo "etcd_connection_details"
echo $etcd_connection_details
echo "data_connection_details"
echo $data_connection_details

# call DistributedServices class - passing it the addresses of all the Palisade services that will be running on the cluster

java -cp /home/hadoop/jars/example-model-*-shaded.jar \
    uk.gov.gchq.palisade.example.config.DistributedServices $etcd_connection_details http://$private_dns:8080/palisade http://$private_dns:8081/policy http://$private_dns:8082/resource http://$private_dns:8083/user $data_connection_details http://$private_dns:8085/config  http://$private_dns:8080/palisade  $data_connection_details
