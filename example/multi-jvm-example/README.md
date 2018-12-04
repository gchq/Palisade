# Multiple JVM Example

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

The services can be started within Docker containers or ran within a local Tomcat.

To run them in them in a local Tomcat, firstly build the executable jars:

```bash
 ./example/multi-jvm-example/scripts/buildServices.sh
```

Either start them all in a single terminal using:
```bash
 ./example/multi-jvm-example/scripts/startAllServices.sh
```
 Or for better logging and understanding of what is going on you can 
 run REST service in 6 separate terminals. This way the logs for each
 service are split up.
```bash
  ./example/multi-jvm-example/scripts/startResourceService.sh
```
```bash
  ./example/multi-jvm-example/scripts/startPolicyService.sh
```
```bash
  ./example/multi-jvm-example/scripts/startUserService.sh
```
```bash
  ./example/multi-jvm-example/scripts/startPalisadeService.sh
```
```bash
  ./example/multi-jvm-example/scripts/startDataService.sh
```
```bash
  ./example/multi-jvm-example/scripts/startConfigService.sh
```
You will need to wait until all the REST services have successfully started in tomcat. 
You should see 6 messages like:
```
INFO: Starting ProtocolHandler ["http-bio-8080"]
INFO: Starting ProtocolHandler ["http-bio-8081"]
INFO: Starting ProtocolHandler ["http-bio-8082"]
INFO: Starting ProtocolHandler ["http-bio-8083"]
INFO: Starting ProtocolHandler ["http-bio-8084"]
INFO: Starting ProtocolHandler ["http-bio-8085"]
```

To run them in Docker:

If you have made changes to the code since you last built the docker containers then you will need to run:

*NOTE* This will clean all stopped docker services, requiring them to be rebuilt when you next want to start them up. 
```bash
  ./example/multi-jvm-example/scripts/dockerCleanSystem.sh
```

Then you can start up the docker containers:
```bash
  ./example/multi-jvm-example/scripts/dockerComposeUp.sh
```

You can check the containers are available:

```bash
  docker ps
```

You should see 6 containers:

```
CONTAINER ID        IMAGE                                                COMMAND             CREATED             STATUS              PORTS                    NAMES
27e121f284c3        multi-jvm-example-docker-services_policy-service     "catalina.sh run"   30 minutes ago      Up 10 seconds       0.0.0.0:8081->8080/tcp   policy-service
01f13c6350d6        multi-jvm-example-docker-services_data-service       "catalina.sh run"   30 minutes ago      Up 8 seconds        0.0.0.0:8084->8080/tcp   data-service
f4d7e07b8412        multi-jvm-example-docker-services_palisade-service   "catalina.sh run"   30 minutes ago      Up 9 seconds        0.0.0.0:8080->8080/tcp   palisade-service
c291ff79eecc        multi-jvm-example-docker-services_resource-service   "catalina.sh run"   30 minutes ago      Up 9 seconds        0.0.0.0:8082->8080/tcp   resource-service
1c16c50a6e2d        multi-jvm-example-docker-services_user-service       "catalina.sh run"   30 minutes ago      Up 9 seconds        0.0.0.0:8083->8080/tcp   user-service
831f5b3a86a3        multi-jvm-example-docker-services_config-service     "catalina.sh run"   30 minutes ago      Up 9 seconds        0.0.0.0:8085->8080/tcp   config-service
```

3. Run the example

For local Tomcat:

```bash
 ./example/multi-jvm-example/scripts/run.sh
```

For Docker:

```bash
 ./example/multi-jvm-example/scripts/runDocker.sh
```


This just runs the java class: uk.gov.gchq.palisade.example.MultiJvmExample (or uk.gov.gchq.palisade.example.MultiDockerJvmExample). You can just run this class directly in your IDE.

4. Stop the REST services

For local Tomcat:

```bash
 ./example/multi-jvm-example/scripts/stopAllServices.sh
```

For Docker:

```bash
 ./example/multi-jvm-example/scripts/dockerComposeDown.sh
```
