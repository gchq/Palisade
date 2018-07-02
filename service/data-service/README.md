# Data Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The core API for the data service.

The responsibility of the data service is to take the read request from the
client, request the trusted details about the request from the Palisade
service (what policies to apply, user details, etc). The data service then
loops over the list of resources passing the list of rules that need to be
applied, taken from the palisade service response (instance of a `DataRequestConfig` class) and the
resource to be read to the `DataReader`. The `DataReader` will then
connect to the resource and apply the rules before streaming the data back to
the `DataService` which forwards the data back to the client.