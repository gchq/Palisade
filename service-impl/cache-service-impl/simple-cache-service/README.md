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

## Operation

### Backing Stores

The simple cache service abstracts the details of *how* the data is stored from the deserialisation by splitting the
storage responsibilities off into a `BackingStore`. A backing store is the lowest level of abstraction and only deals
with a flat key space and the storage of byte sequences. A backing store implementation must connect to the data
storage facility and only needs to provide basic guarantees to its behaviour. It must also implement the time to live
functionality required by the interface.

As well as storing the byte array data associated with a key, a backing store must also store the Java class type of the
cached object. How this is performed is left unspecified, however a common way is to add a *second* key to the backing store
whose value stores the name of the class associated with a given key. An example of this can be found in `PropertiesBackingStore`.

### Simple cache service

At the next abstraction layer above the backing store is the `SimpleCacheService` which works independently of the backing
store implementation. The job of the cache service is actually very simple. It takes cache requests and a key and either
stores or retrieves the value associated with it. As stated above, the backing store only needs to implement a flat key
space, so the separation of services is performed at this level. The cache service takes the service class and the given
key and combines them to make a unique key. This key is then passed down to the backing store.

Values are encoded into byte arrays by way of a cache codec. Various cache codecs are used by the cache service (see below)
to serialise and deserialise Java objects into byte arrays. Custom ones can also be registered by way of the codec registry.

### Cache codec registry

A simple cache service has an associated cache codec registry. The registry maintains pairs of matching functions that
may serialise and deserialise objects of specific types into byte arrays. This allows for different types to be serialised
in different ways thereby enabling the most efficient form of serialisation for an object type to be used as well as
allowing customisation of the process. When the cache receives a request to store a value of type T, it first consults the
registry to see if a codec pair for T has been registered. If not then a default serialiser is used. A similar process
is followed for retrieving objects from the cache.

By default, several codecs are registered. Custom codecs are provided for byte arrays and String objects since these can
be efficiently serialised (the byte array codec is simply the identity function). A default implementation that uses
JSON serialisation is provided for all other object types. If a particular type of object that has a more efficient
serialisation method available is going to regularly placed in the cache, then it is encouraged to provide a custom
codec and register it with the codec registry.
