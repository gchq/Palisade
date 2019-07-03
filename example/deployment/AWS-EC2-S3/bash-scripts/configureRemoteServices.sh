#!/bin/bash

# This script will configure the Palisade Config service (which needs to be already running) so that it knows the addresses of the various Palisade services running on an ec2 instance
# The configuration includes the private dns address of the ec2 server (rather than default of localhost), so that a client running on a different ec2 instance can connect

private_dns=`hostname -f`
echo "Hostname: "$private_dns

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
. "$DIR/setScriptPath.sh"

export HADOOP_CONF_PATH=$EXAMPLE/resources/hadoop_s3.xml

# call DistributedServices class - passing it the addresses of all the Palisade services that will be running on the cluster
java -cp "$EXAMPLE"/example-model/target/example-model-*-shaded.jar \
    uk.gov.gchq.palisade.example.config.DistributedServices http://$private_dns:2379 http://$private_dns:8080/palisade http://$private_dns:8081/policy http://$private_dns:8082/resource http://$private_dns:8083/user http://$private_dns:8084/data http://$private_dns:8085/config  http://$private_dns:8080/palisade  http://$private_dns:8084/data
