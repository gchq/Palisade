# MapReduce Example

This example demonstrates using the MapReduce client for Palisade and shows a MapReduce job that makes requests for 2 different users querying a database. The results are passed into the map task in the MapReduce job.

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

- Property redaction - if the user does not have the 'admin' role the 'property' field should be redacted

The example will be run with 2 users:

- Alice is an admin and can see both public and private records

- Bob is a standard user, who can only see public records

When you run the example you will see the data has been redacted accordingly.

To run the example following the steps (from the root of the project):

1. Compile the code
```bash
mvn clean install -Pquick
```

2. Run the example
```bash
 ./example/mapreduce-example/scripts/run.sh [output_path]
```

The shell script will use a default path under `/tmp` if no path is provided.

This just runs the java class: uk.gov.gchq.palisade.example.MapReduceExample. You can just run this class directly in your IDE.
