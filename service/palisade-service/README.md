# Palisade Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The core API for the Palisade service.

The responsibility of the Palisade service is to send off the required auditing
records and collate all the relevant information about a request for data 
(using the other services) and to provide the Data service with the information 
it requires to enforce the policy and apply any user filters.

In order to support multiple Palisade service instances existing in a single deployment of Palisade, a Cache service is
used to share data between instances.