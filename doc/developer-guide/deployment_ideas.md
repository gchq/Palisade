## How might the system be deployed?

#### Micro-service deployment using REST APIs
![REST Deployment](../img/Palisade_example_microservice_deployment.jpg)

This deployment shows how you might have a Palisade service that can use a local audit service component but uses proxies to ask other micro-services for the other information that it requires. Those other micro-services could be running on different machines.
#### Monolith deployment
![Monolith Deployment](../img/Palisade_example_monolith_deployment.jpg)

This deployment shows that it can still works if you want to have all of your services running in the same JVM.