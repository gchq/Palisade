# Local JVM Example

This example demonstrates different users querying an avro file over a REST api running locally in JVMs.

The example runs several different queries by the different users, with different purposes. When you run the example you will see the data has been redacted in line with the rules.  
For an overview of the example see [here](../../README.md).

To run the example locally in JVMs follow these steps (from the root of the project):

1. Compile the code:
    ```bash
    mvn clean install -P example
    ```
 
2.  Build the executable jars:
     ```bash
       ./example/deployment/local-jvm/bash-scripts/buildServices.sh
     ```

3. Start the REST services, each service runs within a dedicated Tomcat instance. Either:

    a. Start them all in a single terminal using:
    ```bash
      ./example/deployment/local-jvm/bash-scripts/startAllServices.sh
    ```
    
    b. Or for better logging and understanding of what is going on you can
 run REST services in separate terminals. This way the logs for each
 service are split up:
 
    First start up the etcd service:
    ```bash
      ./example/deployment/local-jvm/bash-scripts/startETCD.sh
    ```
    The config service should be started next, as the other services depend on it
    ```bash
      ./example/deployment/local-jvm/bash-scripts/startConfigService.sh
    ```
    Then configure the services:
    ```bash
      ./example/deployment/local-jvm/bash-scripts/configureServices.sh
    ```
    Then the remaining Palisade services:
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
    
    Then populate Palisade with the rules for the example data:
    ```bash
      ./example/deployment/local-jvm/bash-scripts/configureExamples.sh
    ```

4. Run the example:
    ```bash
      ./example/deployment/local-jvm/bash-scripts/runLocalJVMExample.sh
    ```
   Or for an easier to read output:
    ```bash
      ./example/deployment/local-jvm/bash-scripts/runFormattedLocalJVMExample.sh
    ```     
    
    This just runs the java class: `uk.gov.gchq.palisade.example.runner.RestExample`. You can just run this class directly in your IDE.

5. Stop the REST services
    ```bash
      ./example/deployment/local-jvm/bash-scripts/stopAllServices.sh
    ```

