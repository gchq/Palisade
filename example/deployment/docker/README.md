# Docker Example

This example demonstrates 2 different users querying a database over a REST api. 

The example data is a text file which can be found in the example-model module and has the following records:

| Property      | Visibility           | Timestamp  |
| ------------- | -------------------- | ---------- |
|  item1a       |   public             | 1          |
|  item1b       |   public             | 10         |
|  item1c       |   public             | 20         |
|  item1d       |   private            | 20         |
|  item2a       |   public             | 1          |
|  item2b       |   public             | 10         |
|  item2c       |   public             | 20         |
|  item2d       |   private            | 20         |


The policies and users have been hardcoded in class: uk.gov.gchq.palisade.example.client.ExampleSimpleClient.

Policy have defined the following rules:

- Age off - the timestamp must be greater than 12

- Visibility - the user must have the correct level of authorisation to for the visibility label

- Property redaction - if the user does not have the 'admin' role the 'property' field should be redacted
  
The example will be run with 2 users:

- Alice is an admin and can see both public and private records

- Bob is a standard user, who can only see public records

When you run the example you will see the data has been redacted accordingly.

To run the example following the steps (from the root of the project):

1. Compile the code
```bash
mvn clean install -P example
```

2. Start the REST services

If you have made changes to the code since you last built the docker containers then you will need to run:

*NOTE* This will clean all stopped docker services, requiring them to be rebuilt when you next want to start them up. 
```bash
 ./example/deployment/docker/bash-scripts/dockerCleanSystem.sh
```

Then you can start up the docker containers:
```bash
 ./example/deployment/docker/bash-scripts/dockerComposeUp.sh
```

You can check the containers are available:

```bash
  docker ps
```

You should see 6 containers:     ??? I see 9 containers ???

```
CONTAINER ID        IMAGE                                                COMMAND             CREATED             STATUS              PORTS                    NAMES
c23a409e29ed        palisade-example_palisade-service   "catalina.sh run"        3 seconds ago       Up 1 second         8080/tcp            palisade-service
b437e1be5d32        palisade-example_policy-service     "catalina.sh run"        3 seconds ago       Up 1 second         8080/tcp            policy-service
5d17713a67ca        palisade-example_config-service     "catalina.sh run"        3 seconds ago       Up 1 second         8080/tcp            config-service
c86bd8543baf        palisade-example_resource-service   "catalina.sh run"        3 seconds ago       Up 1 second         8080/tcp            resource-service
81bccfcce56a        palisade-example_data-service       "catalina.sh run"        3 seconds ago       Up 1 second         8080/tcp            data-service
70a2a1f69908        palisade-example_user-service       "catalina.sh run"        3 seconds ago       Up 2 seconds        8080/tcp            user-service
c0b6cce34df8        quay.io/coreos/etcd                 "/usr/local/bin/etcd…"   3 seconds ago       Up 2 seconds        2379-2380/tcp       etcd
```

3. Run the example

This script will not run if the `dockerComposeUp.sh` script has not been run.

```bash
  ./example/deployment/docker/bash-scripts/runDockerExample.sh
```


This just runs the java class: uk.gov.gchq.palisade.example.multi-jvm-example-docker-runner. You can just run this class directly in your IDE.

4. Stop the REST services


```bash
 ./example/deployment/docker/bash-scripts/dockerComposeDown.sh
```
