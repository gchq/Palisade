# Kubernetes Example

This example demonstrates different users querying an avro file over a REST api running locally in kubernetes containers.

The example runs several different queries by the different users, with different purposes. When you run the example you will see the data has been redacted in line with the rules.  
For an overview of the example see [here](../../README.md)

### Prerequisites for running in docker 
As well as docker, this example also requires kubernetes

To run the example locally in docker containers follow these steps (from the root of the project):

1. Compile the code:
    ```bash
    mvn clean install -P example
    ```

2. Start the REST services:

    If you have made changes to the code since you last built the docker containers then you will need to run:
    
    *NOTE* This will clean all stopped docker services, requiring them to be rebuilt when you next want to start them up. 
    ```bash
     ./example/deployment/local-docker/bash-scripts/dockerCleanSystem.sh
    ```

    Then you can start up the docker containers to create the docker images:
    ```bash
     ./example/deployment/local-docker/bash-scripts/dockerComposeCreateOnly.sh
    ```


    Then you can start the kubernetes cluster:
    ```
    ./example/deployment/local-k8s/bash-scripts/buildServices.sh
    ```
    You can check the pods are available:
    
    ```
    kubectl get pods
    ```

    After a while you should see the liveness and readiness probes indicating all is well - see the example below:

    ```
Palisade/example/deployment  gh-335-local-k8s ✗                                                                 46m ◒
▶ kubectl get pods
NAME                                READY     STATUS      RESTARTS   AGE
config-service-b8fb57cbc-z6pgg      1/1       Running     0          8m
configure-services-z8p7w            0/1       Completed   0          8m
data-service-59fd76797d-8j8hp       1/1       Running     0          7m
etcd-84dbfdbc86-7chb6               1/1       Running     4          8m
palisade-service-68b4954ddf-kxmqh   1/1       Running     0          8m
policy-service-7798b79bd7-9qhpd     1/1       Running     0          8m
resource-service-766cdbc97d-79sjx   1/1       Running     0          8m
user-service-844f86675c-gc2g8       1/1       Running     0          8m


    ```
3. Run the example:

    This script will not run if the `dockerComposeUp.sh` script has not been run or the buildServices.sh script
    
    ```bash
      ./example/deployment/local-k8s/bash-scripts/bash-scripts/example.sh
    ```

4. Stop the REST services:

    ```bash
    ./example/deployment/local-k8s/bash-scripts/deleteServices.sh
    ./example/deployment/local-docker/bash-scripts/dockerCleanSystem.sh
    ```
