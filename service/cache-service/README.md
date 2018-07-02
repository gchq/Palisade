# Cache Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The core API for the cache service.

The purpose of the cache service is to store the information that the data
service will require based on a `registerDataRequest()` made to the Palisade
service. The reason this is required is because you might have multiple
instances of the Palisade service running (for resilience) and the registration request might
go to a different Palisade service instance to the `getDataRequestConfig()`.

The data in the cache should be maintained by a time to live (TTL) value rather than
manually removing after the get request as any scalable deployment would likely make
multiple requests to the cache due to many data services working on a subset
of the list of resources in parallel.