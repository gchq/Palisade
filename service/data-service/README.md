# Data Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The core API for the data service.

The data service has one method read().

The responsibility of the data service is to take the read request from the
client, request the trusted details about the request from the Palisade
service (what policies to apply, user details, context, etc). The data service then
passes that information to the `DataReader` which is then responsible for connecting to the resource, 
deserialising the data, applying the rules and then serialising the data ready to be sent to the client.
The data service is also responsible for ensuring the relevant audit logs are generated.