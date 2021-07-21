<!---
Copyright 2018-2021 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--->

# Palisade Clients
This section gives some insight into how clients could be used as the entry point for users to access data via Palisade.

## Current

### Reading a stream of data
By default, the Data Service presents an HTTP stream of binary data to the client on request.

A code sample using the `client-akka` package to read all AVRO resources in a dataset (without the client interpreting the AVRO data):
```java
void printInputStream(InputStream is) {
    new BufferedReader(new InputStreamReader(is))
        .lines()
        .forEach(line -> System.out.println(line));
}

void doRequest() {
    String token = client.register("Alice", "file:/palisade-data-store/dataset-1/", "").toCompletableFuture().join()
    Source<LeafResource, ?> avroResources = client.fetch(token).filter(resource -> resource.getType().equals("AVRO"));
    Source<InputStream, ?> avroRecords = avroResources.flatMapConcat(resource -> client.read(token, resource));
    avroRecords.runWith(Sink.forEach(this::printInputStream), materializer);
}
```
In reality, this `Source<InputStream, ?>` would be interpreted by the client using an AVRO deserialiser.

Similarly, Palisade has a `client-java` package which implements the Palisade Service/Filtered-Resource Service/Data Service protocol using the java standard-library, instead exposing a `java.util.concurrent.Flow.Publisher` and `java.io.InputStream`.

### Using the 'cat' command line tool
It should be possible to use command line tools like 'cat' to be able to view files that are being protected by Palisade. 
To do that, we would need to write a client that mimics the behaviour of the 'cat' command but routing the request for data via Palisade. 
Then you could alias 'cat' to run that client code. 
Therefore, to the end user, there is very little difference to how they would normally use 'cat' if they did not have the data access policy restrictions.

When using Palisade, there is some extra context to be managed, and a 'dumb' cat-client might be quite verbose (the user would have to keep track of tokens themself).
Instead, there are two implementations of this style of client.

The first of these, the `client-shell` package, is a simple shell text user interface with commands similar to `cat`, `cd`, and `ls`.
This aims to demonstrate to a developer how Palisade works, mapping each part of the protocol between the client and server to a different UNIX-like command.
As commands are executed, the data returned is printed out in a human-readable fashion.

The second of these, the `client-fuse` package, is a connector between Palisade and the FUSE interface, which will mount the results of a Palisade query as a filesystem.
This aims to allow a quick-and-easy way to provide compatibility with a whole host of existing tools, as well as present an interactive graphical way to explore returned data.
Of course, this client also allows use of many existing UNIX CLI tools, such as `ls` and `cat`, but also more complex tools such as `sed` or `grep`.
This approach is not the most performant, especially when querying many thousands of resources, but should be more than enough for proof-of-concept and demonstrative purposes.


### Using the S3 Client
It is possible, via the [S3 Client](https://github.com/gchq/Palisade-clients/tree/develop/client-s3) to read resources stores in AWS S3 data stores, allowing for a full AWS implementation of Palisade. 
Upon deployment of Palisade using the S3 client, the relevant services are configured to expect data stored in S3, and are loaded with the relevant serialisers required to deseralise the returned LeafResource to return to the client.  

### Reading a stream of data using Apache Spark
You can easily adopt the S3 Client to read data from Apache Spark.  
Given a Spark job running against AWS S3 as follows:

```scala
spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "http://s3.eu-west-2.amazonaws.com/")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.aws.credentials.provider", "com.amazonaws.auth.DefaultAWSCredentialsProviderChain")
val nonrecursive = scala.io.Source.fromFile("/schema/nonrecursive.json").mkString
spark.read.format("avro").option("avroSchema", nonrecursive).load("s3a://palisade-application-dev/data/remote-data-store/data/employee_file0.avro").show()
```

_Note that we use a modified non-recursive AVRO schema `/schema/nonrecursive.json` (this excludes the managers field) as recursive schema are not compatible with Spark SQL._

Adapt the Spark job to run against the Palisade S3 client (ensure the client is running and correctly configured).
This short snippet requires `curl`, but otherwise works wholly within `spark-shell` and the `s3` and `avro` libraries as the previous did:

```scala
import sys.process._;
// User 'Alice' wants 'file:/data/local-data-store/' directory for 'SALARY' purposes
// We get back the token '09d3a677-3d03-42e0-8cdb-f048f3929f8c', to be used as a bucket-name
val token = (Seq("curl", "-X", "POST", "http://localhost:8092/register?userId=Alice&resourceId=file%3A%2Fdata%2Flocal-data-store%2F&purpose=SALARY")!!).stripSuffix("\n")
Thread.sleep(5000)

spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "localhost:8092/request")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.path.style.access", "true")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.connection.ssl.enabled", "false")
// These are not interpreted or validated by Palisade, but Spark requires them to be non-null
spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "accesskey")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "secretkey")
// spark.read.format("avro").load("s3a://" + token + "/with-policy/employee_small.avro").show()
val nonrecursive = scala.io.Source.fromFile("/schema/nonrecursive.json").mkString
spark.read.format("avro").option("avroSchema", nonrecursive).load("s3a://" + token + "/data/employee_file0.avro").show()
```
