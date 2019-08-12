# Potential Palisade Clients

This section gives some insight into how clients could be used as the entry point for users to access data via Palisade.

## Reading a stream of data using Apache Spark
If you had a data stream in Kafka, you would normally be able to access it using kafka connectors in Java or Python, however it would not apply the data filtering/transformations that are required by the data policies.
Therefore, we want to be able to provide the user with as similar an experience as possible so the user hardly realises any difference.

To do that we would want the user to start the job off by running the following: 

```java
spark.readStream
    .option("purpose", "example purpose")
    .format("uk.gov.gchq.palisade")
    .load("data_set")
    .select("colA", "colB")
    .show()
```

We would expect that Spark would create a filter to be pushed down to the data reader that says it only wants "colA" and "colB".

The Spark client for Palisade would create a user (based on who is running the command) and take the resource, purpose and filter to register a data request with the Palisade service. Then the Spark client would then take the response of that request and split the list of resources over the number of executors Spark is running and get each executor to request access to the subset of resources assigned to that executor.

The data service receives the request for data and passes that request to the palisade service to validate the request and to get the data access policies that apply to those resources. Then the data service can read the resource applying the column selection (columns requested plus any columns required to apply the policy) then apply the policies, transform the data so only the two requested columns are returned and then stream the data back to the executor that made the request.

Then the Spark client can format the stream of data into a DataFrame so that the standard Spark code can do the rest of the request.

Therefore as far as the user is concerned they just had to add the option and use the different format, as they would not be able to access the data via any of the other formats. This keeps the API similar to Spark's standard read API. 

## Using the 'cat' command line tool
It should be possible to use command line tools like 'cat' to be able to view files that are being protected by Palisade. 
To do that we would need to write a client that mimics the behaviour of the 'cat' command but routing the request for data via Palisade. 
Then you could alias 'cat' to run that client code. 
Therefore to the end user there is again very little difference to how they would normally use 'cat' if they did not have the data access policy restrictions.

See [cat-client](client-impl/cat-client/README.md) for the implementation of this client.

## Creating an S3 client endpoint
It should be possible to create an S3 endpoint that allows any out of the box data processing technology that supports S3 to route requests via Palisade. 
This mechanism would require a way to register your user supplied information such as the purpose for your query to a separate service which provides you a token to embed in the resource URL.
Then the client would be able to go to strip out the context token and retrieve the contextual information to be used by Palisade.