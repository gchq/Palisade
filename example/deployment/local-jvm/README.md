# Local JVM Example

This example demonstrates different users querying an avro file over a REST api running locally in JVMs.

The queried file is a generated HR dataset of Employee records. Here is the format of the Employee class:  
    private UserId uid;  
    private String name;  
    private String dateOfBirth;  
    private PhoneNumber[] contactNumbers;  
    private EmergencyContact[] emergencyContacts;  
    private Address address;  
    private BankDetails bankDetails;  
    private String taxCode;  
    private Nationality nationality;  
    private Manager[] manager;  

    The manager field is an array of manager, which could potentially be nested several layers deep, in the generated example manager is nested 3 deep.


The policies and users have been hardcoded in class: uk.gov.gchq.palisade.example.client.ExampleSimpleClient.

Policy have defined the following rules:

1. BankDetailsRule - The bankDetails field should be displayed if the user querying the file has the PAYROLL role and the purpose of the query is SALARY  
   In all other cases the bankDetails field should be redacted.

1. DutyOfCareRule - this rule is concerned with the contactNumbers and emergencyContacts fields. These fields should be returnred 
    - if the user querying the file has theh HR role and the purpose of the query is DUTY_OF_CARE
    - also if the user querying the file is the line manager of the Employee record being queried and the purpose of the query is DUTY_OF_CARE
   In all other cases these fiels should be redacted.


1. NationalityRule -

1. ZipCodeMaskingRule -
  
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
   ./example/deployment/local-jvm/bash-scripts/buildServices.sh
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

Then populate Palisade with the rules for the example data
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

