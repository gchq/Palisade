# Local JVM Example

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
 
2.  Build the executable jars

 ```bash
   ./example/deployment/multi-use/bash-scripts/buildServices.sh
 ```

3. Start the REST services


Either start them all in a single terminal using:
```bash
  ./example/deployment/local-jvm/bash-scripts/startAllServices.sh
```
Or for better logging and understanding of what is going on you can
 run REST service in separate terminals. This way the logs for each
 service are split up:
 
First start up the etcd service
```bash
  ./example/deployment/local-jvm/bash-scripts/startETCD.sh
```
The config service should be started next, as the other services depend on it
```bash
  ./example/deployment/local-jvm/bash-scripts/startConfigService.sh
```
Then configure the services
```bash
  ./example/deployment/local-jvm/bash-scripts/configureServices.sh
```
Then the rermaining Palisade services
```bash
  ./example/deployment/local-jvm/bash-scripts/startResourceService.sh
```
```bash
  ./example/deployment/local-jvm/bash-scripts/startPolicyService.sh
```
```bash
  ./example/deployment/local-jvm/bash-scripts/startUserService.sh
```
```bash
  ./example/deployment/local-jvm/bash-scripts/startPalisadeService.sh
```
```bash
  ./example/deployment/local-jvm/bash-scripts/startDataService.sh
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


Then populate the example data - so that the examples will have something to query
```bash
  ./example/deployment/local-jvm/bash-scripts/configureExamples.sh
```


4. Run the example

```bash
  ./example/deployment/local-jvm/bash-scripts/runLocalJVMExample.sh
```


This just runs the java class: uk.gov.gchq.palisade.example.MultiJvmExample. You can just run this class directly in your IDE.

5. Stop the REST services

```bash
  ./example/deployment/local-jvm/bash-scripts/stopAllServices.sh
```

