# HashMap User Service

A simple implementation of a User Service that simply stores the users
in a ConcurrentHashMap. By default the map is static so it will
be shared across the same JVM.

Since this implementation is only designed for example purposes, then it doesn't connect to any backend account service. All users known to the hash map user service implementation are ones that have been explicitly added via an `addUser` call.