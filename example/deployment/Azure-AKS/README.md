# Azure AKS Example

This example demonstrates how to deploy Palisade within the Azure Kubernetes service using the Azure DevOps pipelines to automate the deployment process and to run the example

The pre-requisites of this example are:
- Azure subscription
- Azure service principle with contributor access permissions on your subscription
- Azure DevOps account


##Configuring the Azure DevOps account: 
[devOps](./ConfigureAzureDevOPS.md)

##Connecting to the example
1. Open the Microsoft Azure Cloud Shell by following these [instructions](https://docs.microsoft.com/en-us/azure/cloud-shell/quickstart)
1. The following [documentation](https://docs.microsoft.com/en-us/azure/aks/kubernetes-walkthrough) is useful for explaining hoe create an Azure Kubernetes Cluster
1. Run the following command in the command shell:
( This command downloads credentials and configures the Kubernetes CLI to use them.)
```bash
user@Azure:~$ az aks get-credentials --resource-group palisade --name palisade-aks
Merged "palisade-aks" as current context in /home/nigel/.kube/config
user@Azure:~$
```
1. If all is well, you can now issue the following command and see the status output:

```bash
user@Azure:~$ kubectl get pods
NAME                                READY   STATUS      RESTARTS   AGE
config-service-79868dd5b6-9l2md     1/1     Running     1          24h
configure-services-g2wxm            0/1     Completed   0          24h
data-service-64f596c6b9-5m2jl       1/1     Running     1          24h
data-service-64f596c6b9-9wlgf       1/1     Running     1          160m
data-service-64f596c6b9-ctj24       1/1     Running     1          24h
data-service-64f596c6b9-nchdr       1/1     Running     1          160m
data-service-64f596c6b9-td8lz       1/1     Running     1          24h
etcd-fcdf6b9f4-xlsgp                1/1     Running     1          24h
palisade-service-c98d8b7c-hkq5h     1/1     Running     1          160m
policy-service-6594d845db-8h4c6     1/1     Running     1          160m
resource-service-79487d98d8-z7dff   1/1     Running     1          24h
user-service-96d9dd769-x6fxb        1/1     Running     1          24h
user@Azure:~$
```

