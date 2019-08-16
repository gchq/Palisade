# Creating a Jenkins instance running under Kubernetes on AWS on an EC2 instance

## Inital setup
1. Create an EC2 instance / start with a Ubuntu Server 18.04 LTS
1. In the case of AWS choose a sufficiently large EC2 instance (t2.xlarge)
1. Configure Instance Details - set auto assign public IP
1. Configure security group to select SSH access for ports 22 and 31750 from your ip address
1. Configure an existing key pair to access
1. Click on launch instance
1. Create an elastic block store volume (80GB) and note down the Volume ID 
1. Connect to the EC2 instance as follows and select yes:
```bash
ssh -i "your pem.pem" ubuntu@ec2-XX-XXX-XXX-XX.eu-west-1.compute.amazonaws.com
The authenticity of host 'ec2-``XX-XXX-XXX-XX.eu-west-1.compute.amazonaws.com (XX.XXX.XXX.XX)' can't be established.
ECDSA key fingerprint is SHA256:6W5mciVvBve7QM9HdKtgpqQOgULt/J5So4a/9tuBOCI.
Are you sure you want to continue connecting (yes/no)? 
```


## Package Installation

### Install microk8s
MicroK8s is a snap lightweight kubernetes implementation: https://microk8s.io/docs/
Run the following commands to install microk8s and verify that it's working:

```bash
ubuntu@ip-XXX-XX-XX-XXX:~$ sudo snap install microk8s --classic
ubuntu@ip-XXX-XX-XX-XXX:~$ microk8s.kubectl get nodes
ubuntu@ip-XXX-XX-XX-XXX:~$ microk8s.kubectl get services
ubuntu@ip-XXX-XX-XX-XXX:~$ sudo snap alias microk8s.kubectl kubectl
ubuntu@ip-XXX-XX-XX-XXX:~$ kubectl get nodes
```

Check the status of the microk8s cluster:
```bash
ubuntu@ip-XXX-XX-XX-XXX:~$ microk8s.status
microk8s is running
addons:
knative: disabled
jaeger: disabled
fluentd: disabled
gpu: disabled
storage: disabled
registry: disabled
rbac: disabled
ingress: disabled
dns: disabled
metrics-server: disabled
linkerd: disabled
prometheus: disabled
istio: disabled
dashboard: disabled
```
We will now enable the storage and dns add ons:

```bash
ubuntu@ip-XXX-XX-XX-XXX:~$ microk8s.enable storage
ubuntu@ip-XXX-XX-XX-XXX:~$ microk8s.enable dns

```

Rerun microk8s.status and ensure that storage and dns are now enabled


Install docker
```bash
ubuntu@ip-XXX-XX-XX-XXX:~$ sudo snap install docker
```

Install Helm
```bash
ubuntu@ip-XXX-XX-XX-XXX:~$ sudo snap install helm --classic
```

Run helm init to install tiller:
```bash
ubuntu@ip-XXX-XX-XX-XXX:~$ helm init
```

Copy the following files from the repo into /home/ubuntu
jenkins-chart.yaml
jenkins-pv.yaml
jenkins-pvc.yaml
values.yaml

Edit jenkins-pv.yaml and change the volume id to that saved above

Run the following:

```bash
ubuntu@ip-XXX-XX-XX-XXX:~$ kubectl apply -f jenkins-pv.yaml
ubuntu@ip-XXX-XX-XX-XXX:~$ kubectl apply -f jenkins-pvc.yaml
ubuntu@ip-XXX-XX-XX-XXX:~$ kubectl get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM                 STORAGECLASS        REASON   AGE
jenkins-pv                                 5Gi        RWO            Retain           Available                         standard                     5h50m
pvc-8c50eba5-9703-4e00-b2b3-xxxxxxxxxxxx   4Gi        RWO            Delete           Bound       default/jenkins-pvc   microk8s-hostpath            5h50m
```


Run the following which will enable Jenkins:

```bash
helm install --name jenkins -f jenkins-chart.yaml stable/jenkins --values values.yaml
```

wait for the pods to come into service:

```bash
ubuntu@ip-XXX-XX-XX-XXX:~$ kubectl get pods
NAME                       READY   STATUS    RESTARTS   AGE
jenkins-68f54d45fb-fkl4s   1/1     Running   0          79m
```

Run the following to get the connetion details:

```bash
ubuntu@ip-XXX-XX-XX-XXX:~$ kubectl get secret --namespace default jenkins -o jsonpath="{.data.jenkins-admin-user}" | base64 --decode; echo
ubuntu@ip-XXX-XX-XX-XXX:~$ kubectl get secret --namespace default jenkins -o jsonpath="{.data.jenkins-admin-password}" | base64 --decode; echo
ubuntu@ip-XXX-XX-XX-XXX:~$ export NODE_PORT=$(kubectl get --namespace default -o jsonpath="{.spec.ports[0].nodePort}" services jenkins)
ubuntu@ip-XXX-XX-XX-XXX:~$ export NODE_IP=$(kubectl get nodes --namespace default -o jsonpath="{.items[0].status.addresses[0].address}")
ubuntu@ip-XXX-XX-XX-XXX:~$ echo http://$NODE_IP:$NODE_PORT/login
```

You should now be able to access the jenkins server 

![./jenkinsRunning.png](./jenkinsRunning.png)