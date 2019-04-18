# HR Data Generator

To generate some dummy HR data run this command from the base directory of the oinstall

java -cp example/example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar uk.gov.gchq.palisade.example.hrdatagenerator.CreateData    args

    ```bash
      java -cp ./example/example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar uk.gov.gchq.palisade.example.hrdatagenerator.CreateData    args
    ```

pass 3 arguements (and an optional 4th arguement):
    output directory path 
    number of employees to generate 
    number of output files 
    optionally number of threads
    
    
e.g. to genarate 1,000,000 employee records, spread over 15 files, running the program with 15 threads, and writing the output files to /data/employee:

    ```bash
      java -cp ./example/example-model/target/example-model-0.2.1-SNAPSHOT-shaded.jar uk.gov.gchq.palisade.example.hrdatagenerator.CreateData   /data/employee 1000000  15  15
    ```
    