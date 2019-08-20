# Cat Client

The cat client is designed to provide a capability similar to the linux terminal 'cat' command for printing out to the terminal the contents of a file.

To use the cat client you will need to have a running deployment of Palisade. An example would be to run through the [local-jvm example](../../example/deployment/local-jvm/README.md) 
but instead of running the runLocalJVMExample.sh, you should run the following command: 
```bash
PALISADE_REST_CONFIG_PATH="$(pwd)/example/example-model/src/main/resources/configRest.json" java -cp $(pwd)/client-impl/cat-client/target/cat-client-*-shaded.jar uk.gov.gchq.palisade.client.CatClient Alice file://$(pwd)/example/resources/employee_file0.avro SALARY
```

That command makes it easy for a system admin to create an alias for users such that it hides most of the complication of the command and just leaves the user to specify the resource to access and the purpose for accessing it. For example the system administrator could set the following alias:
```bash
alias cat="PALISADE_REST_CONFIG_PATH=$(pwd)/example/example-model/src/main/resources/configRest.json java -cp $(pwd)/client-impl/cat-client/target/cat-client-*-shaded.jar uk.gov.gchq.palisade.client.CatClient "'$(whoami)'
```

Then a user could run the command:
```bash
cat file://$(pwd)/example/resources/employee_file0.avro SALARY
```