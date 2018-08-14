## Design Principles
* Reduce the work required to adopt new data processing and data storage technologies.
* The service and common modules should remain technology agnostic.
* Palisade should scale horizontally and enable monitoring, with the intention being to enable auto-scaling and auto-healing of each micro-service.
* Palisade should also make it easy to debug by enabling the tracing of a request through all the micro-services that make up Palisade.
* Minimise serialisation/de-serialisation by making use of Apache Arrow where sensible.