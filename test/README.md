# 

We test the libraries in the DeepHealth's Kubernetes cluster and verify the correct functioning within the infrastructure. Two PODs are built based on two examples proposed in the X repository.

## Requirements

- Dataset: it's used [ISIC/Segmentation dataset](https://github.com/deephealthproject/use_case_pipeline/blob/master/README.md)

## Objectives

- Check if it is possible to start two use-case examples within a DeepHealth cluster.

## Before you begin

- You need to either have a dynamic Persistent Volume provisioner with a default StorageClass, or statically provision Persistent Volumes yourself to satisfy the Persistent Volume Claim used here.

## Configure a Pod to use a PV for Local Storage

First, create a file with Local Storage: 

**dh-sc.yaml**
```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: local-storage
provisioner: k8s.io/minikube-hostpath
volumeBindingMode: WaitForFirstConsumer
```
-  
