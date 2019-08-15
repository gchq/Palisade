# HR Data Generator

This module contains the code for the HR Data examples. This includes generator code that can produce AVRO files of
synthetic HR data.

To use the generator, then from the Palisade root directory run:

```mvn clean install -P example```

then to start the generator:

```./example/deployment/bash-scripts/createHRData.sh PATH EMPLOYEES FILES [THREADS]```

where PATH is the relative path to generate the files, EMPLOYEES is the number of employee records to create, FILES
is the number of files to spread them over and the THREADS (optional) specifies the number of threads to use.

For example to generate 1,000,000 employee records, spread over 15 files, running the program with 15 threads, and writing the output files to /data/employee:

```bash
./example/deployment/bash-scripts/createHRData.sh /data/employee 1000000 15 15
```
