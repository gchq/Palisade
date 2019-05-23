# HR Data Generator

This module contains the code for the HR Data examples. This includes generator code that can produce AVRO files of
synthetic HR data.

To use the generator, then from the Palisade root directory run:

```mvn clean install -Pexample```

then to start the generator:

```./example/deployment/bash-scripts/createHRData.sh PATH EMPLOYEES FILES [THREADS]```

where PATH is the relative path to generate the files, EMPLOYEES is the number of employee records to create, FILES
is the number of files to spread them over and the THREADS (optional) specifies the number of threads to use.