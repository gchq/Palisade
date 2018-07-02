# HashMap Resource Service

A simple implementation of a Resource Service that simply stores the 
resources in a ConcurrentHashMap. More precisely it uses multiple maps to allow the resources to be indexed by resource, id, type and format.

By default the map is static so it will be shared across the same JVM.
