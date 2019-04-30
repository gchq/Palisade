# MapReduce Example

This example demonstrates using the MapReduce client for Palisade and shows a MapReduce job that makes requests for 2 different users querying a database. The results are passed into the map task in the MapReduce job.

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

2. Run the example using a file from the local file system
```bash
 ./example/mapreduce-example/scripts/run.sh [output_path]
```

The shell script will use a default path under `/tmp` if no path is provided.

This just runs the java class: uk.gov.gchq.palisade.example.MapReduceExample. You can just run this class directly in your IDE.

3. Run the example using a file from a S3 bucket (hosted on a S3 server in a local Docker container)
```bash
./example/mapreduce-example/scripts/runS3.sh
```

The shell script will start a Docker container running a minio S3 server and upload a file into a S3 bucket on the server.
The script will then call the `run.sh` script above, but use the s3a:// scheme so that Palisade will retrieve the file
from the S3 server. Finally, the Docker containers are terminated.
