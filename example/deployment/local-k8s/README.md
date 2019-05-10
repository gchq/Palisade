# Kubernetes Example

This example demonstrates different users querying an avro file over a REST api running locally in kubernetes containers.

The example runs several different queries by the different users, with different purposes. When you run the example you will see the data has been redacted in line with the rules.  
For an overview of the example see [here](../../README.md)

### Prerequisites for running in kubernetes 
As well as docker, this example also requires Kubernetes. Kubernetes is now bundled as part of docker. The following
screenshows shows the Docker Kubernetes preferences:

![Alt text](./k8sPreferences.png?raw=true "Kubernetes preferences")


##### N.B. If you have "Show system containers (advanced) ticked, you will be unable to run the etcd service on port 3279"


<font color="green"> Some green text </font>

To run the example locally in docker containers (under kubernetes) follow these steps (from the root of the project):

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
    ```bash
    ./example/deployment/local-k8s/bash-scripts/buildServices.sh
    ```

    You can check the pods are available:    
    ```bash
    kubectl get pods
    ```

    After a while you should see the liveness and readiness probes indicating all is well - see the example below:
    ```bash    
    ▶ kubectl get pods
    NAME                                READY     STATUS      RESTARTS   AGE
    config-service-5489cbc95f-b2js5     1/1       Running     0          2m
    configure-example-7n8pr             0/1       Completed   0          2m
    configure-service-6tkzs             0/1       Completed   0          2m
    data-service-865784cd6-xz85z        1/1       Running     0          2m
    etcd-6d6f4d5d66-85lbb               1/1       Running     0          2m
    palisade-service-6d8fc58bb9-rm2vm   1/1       Running     0          2m
    policy-service-6c7779db95-w9k5k     1/1       Running     0          2m
    resource-service-764644dffd-dbnsj   1/1       Running     0          2m
    user-service-76747d569-s24hd        1/1       Running     0          2m
    ```
    You can verify that ingress is working correctly by running the following commands:

    ```bash    
    curl -kL http://localhost/config/v1/status && curl -kL http://localhost/palisade/v1/status &&
    curl -kL http://localhost/data/v1/status
    ```
    
3. Run the test example with:
    ```bash
    ./example/deployment/local-k8s/bash-scripts/runExample.sh
    ```
    
4. Stop the REST services:
    ```bash
    ./example/deployment/local-k8s/bash-scripts/deleteServices.sh
    ./example/deployment/local-docker/bash-scripts/dockerCleanSystem.sh
    ```