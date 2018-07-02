# Multiple JVM Example

This example demonstrates 2 different users querying a database over a REST api. 

The database has been hardcoded in class: uk.gov.gchq.palisade.example.data.ExampleSimpleDataReader and has been loaded with the following records:

| Value         | Visibility           | Timestamp  |
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
  
The example will be run with 2 users:

- Alice is an admin and can see both public and private records

- Bob is a standard user, who can only see public records

When you run the example you will see the data has been redacted accordingly.

To run the example following the steps (from the root of the project):

1. Compile the code
```bash
mvn clean install -Pquick
```

2. Start the REST services
Either start them all in a single terminal using:
```bash
 ./example/multi-jvm-example/scripts/startAllServices.sh
```
 Or for better logging and understanding of what is going on you can 
 run REST service in 5 separate terminals. This way the logs for each 
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
You will need to wait until all the REST services have successfully started in tomcat. 
You should see 5 messages like:
```
INFO: Starting ProtocolHandler ["http-bio-8080"]
INFO: Starting ProtocolHandler ["http-bio-8081"]
INFO: Starting ProtocolHandler ["http-bio-8082"]
INFO: Starting ProtocolHandler ["http-bio-8083"]
INFO: Starting ProtocolHandler ["http-bio-8084"]
```

3. Run the example
```bash 
 ./example/multi-jvm-example/scripts/run.sh
```
This just runs the java class: uk.gov.gchq.palisade.example.MultiJvmExample. You can just run this class directly in your IDE.

4. Stop the REST services
```bash
 ./example/multi-jvm-example/scripts/stopAllServices.sh
```
