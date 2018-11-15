# HashMap backing store

A simple implementation of a backing store that simply stores the key value pairs
in a ConcurrentHashMap. By default the ConcurrentHashMap is static so it will
be shared across the same JVM.
