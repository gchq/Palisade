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

## Reading a stream of data
By default, the data-service presents a HTTP stream of binary data to the client on request.

```java
session
    .createQuery("file:/data/local-data-store/", Map.of("purpose", "SALARY"))
    .execute()
    .thenApply(QueryResponse::stream)
```


## Using the 'cat' command line tool
It should be possible to use command line tools like 'cat' to be able to view files that are being protected by Palisade. 
To do that we would need to write a client that mimics the behaviour of the 'cat' command but routing the request for data via Palisade. 
Then you could alias 'cat' to run that client code. 
Therefore to the end user there is again very little difference to how they would normally use 'cat' if they did not have the data access policy restrictions.

## Creating an S3 client endpoint
It should be possible to create an S3 endpoint that allows any out of the box data processing technology that supports S3 to route requests via Palisade. 
This mechanism would require a way to register your user supplied information such as the purpose for your query to a separate service which provides you a token to embed in the resource URL.
Then the client would be able to go to strip out the context token and retrieve the contextual information to be used by Palisade.