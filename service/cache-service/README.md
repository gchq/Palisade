# Cache Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The core API for the cache service.

The purpose of the cache service is to act as a short term caching layer between 
a service and a backing store that ultimately stores the data for the duration 
of the TTL. As such the cache service will usually be part of another services 
process and not shared between different services. 

The cache service has three methods `add()`, `get()` and `list()`.

The data in the cache should be maintained by a time to live (TTL) value rather than
manually removing after the get request as any scalable deployment would likely make
multiple requests to the cache due to many data services working on a subset
of the list of resources in parallel.