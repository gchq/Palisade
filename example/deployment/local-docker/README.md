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
 ./example/deployment/local-docker/bash-scripts/dockerCleanSystem.sh
```

Then you can start up the docker containers:
```bash
 ./example/deployment/local-docker/bash-scripts/dockerComposeUp.sh
```

You can check the containers are available:

```bash
  docker ps
```

You should see 8 containers that will say around and 2 others will disappear once complete (configure-services, configure example):

```
CONTAINER ID        IMAGE                      COMMAND                  CREATED             STATUS                 PORTS               NAMES
d793e5c6c3af        example_rest-redirector    "java -cp /example-r…"   3 hours ago         Up 3 hours                                 rest-redirector
d92dd0517a08        example_data-service       "catalina.sh run"        3 hours ago         Up 3 hours (healthy)   8080/tcp            data-service
d7055357f85a        example_palisade-service   "catalina.sh run"        3 hours ago         Up 3 hours (healthy)   8080/tcp            palisade-service
4c850cfbfa32        example_policy-service     "catalina.sh run"        3 hours ago         Up 3 hours (healthy)   8080/tcp            policy-service
1e2608882e8e        example_resource-service   "catalina.sh run"        3 hours ago         Up 3 hours (healthy)   8080/tcp            resource-service
dc5b2f7d9b90        example_user-service       "catalina.sh run"        3 hours ago         Up 3 hours (healthy)   8080/tcp            user-service
3983e13a1972        example_config-service     "catalina.sh run"        3 hours ago         Up 3 hours (healthy)   8080/tcp            config-service
9dd2cf3b4223        example_etcd               "/usr/local/bin/etcd…"   3 hours ago         Up 3 hours (healthy)   2379-2380/tcp       etcd
```

3. Run the example

This script will not run if the `dockerComposeUp.sh` script has not been run.

```bash
  ./example/deployment/local-docker/bash-scripts/runDockerExample.sh
```


This just runs the java class: uk.gov.gchq.palisade.example.multi-jvm-example-docker-runner. You can just run this class directly in your IDE.

4. Stop the REST services


```bash
 ./example/deployment/docker/bash-scripts/dockerComposeDown.sh
```
