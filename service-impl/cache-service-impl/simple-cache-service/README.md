# Simple cache service

A simple implementation of a cache service that applies the serialisation and 
deserialisation of the key value pairs that a service wants to cache. This simple 
cache service can then persist those key value pairs by using a backing store that 
persists the data. The cache service supports a TTL which is the TTL of the data 
set to the backing store.

In future it is planned that this class will also maintain a short term 
(few minute) cache of data pulled from the backing store to speed up requests and 
prevent hitting the backing store (which may be reading from disk over a network) 
too frequently.
