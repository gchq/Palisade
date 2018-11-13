# Rest Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The rest service provides an inter-service communication mechanism, which the 
proxy-rest-service being used to send messages out of one service and the 
core-rest-service being used to receive messages from the proxy. These modules 
do need to be extended for each service to specify the public APIs, however this 
prevents lots of duplication of code.