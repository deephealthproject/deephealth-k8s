# Distributed/Parallel ECVL's inference on Cloud

We tested the use case from ECVL libraries in the DeepHealth's Kubernetes cluster. The goal is to launch in a distributed/parallel way the inference. JOBs are building based on an examples proposed in the **Use Case Pipeline** repository.

## Objectives

- From an AI model in "bin" format, run the inference of the use case in different pod/machines.

## Before you begin

- Dataset used: it's used [ISIC/Segmentation dataset](https://github.com/deephealthproject/use_case_pipeline/blob/master/README.md) [1]

- [Skin lesion classification training example](https://github.com/deephealthproject/pyecvl/blob/master/examples/use_case_pipeline/skin_lesion_classification_training.py)

- [Skin lesion segmentation inference example](https://github.com/deephealthproject/pyecvl/blob/master/examples/use_case_pipeline/skin_lesion_segmentation_inference.py)

- **NFS storage** (it comes standard in DeepHealth's cluster). NFS server we can share folders over the network and allowed clients or system can access those shared folders and can use them in their applications.
  
- You need to either have a dynamic Persistent Volume (PV) provisioner with a default StorageClass, or statically provision Persistent Volumes yourself to satisfy the Persistent Volume Claim (PVC) used here.

## Split Dataset

The idea is to divide the Dataset in as many parts as you want and each one of them is executed in a different POD, either inside the same machine or in different machines in a parallel way.

The engine load YAML file (as in [1]) and to divide in n-parts with the same structured. To do this, it calculates the percentage of elements assigned to training, validation and testing, and assigns the same percentage to each sub-YAML. 

An example of how this works would be if we had 2750 images. If we wanted to divide it into 5 parts, we would have 5 sub-YAMLs with 550 images each. In the case of dividing it into 4 parts, we had 687.5 images. Since this number is not integer, more images will be assigned to the last partition, that is, we would have three partitions with 687 images and the last one with 689 images.

The image of this engine can be found in DeepHealth's Docker HUB repository.

## Configure a JOBs to use a PV for NFS Storage

Four YAML files have been constructed for the execution of this example:

- **01-dh-pv-nfs.yaml**: Persistent Volume and Persistent Volume Claim related to NFS storage.
- **02-dh-pod-training.yaml**: Pod that runs the model training
- **03-dh-job-splityaml.yaml**: Pod associated with the execution of the engine that will perform the dataset split (sub-YAMLs).
- **04-dh-job-inference.yaml**: From the sub-YAMLs, a Job will be launched which will run the inference example.
- **05-dh-job-clean-up.yaml**: Clean all sub-YAMLs file from NFS storage.

On the other hand, we have two scripts associated only with the inference process:

- **script/inference-workflow.config**: configuration required for the split dataset.
- **script/inference-workflow.sh**: From a model, a workflow was built where each sub-YAML will be executed in different Pods, and if necessary, in different machines. 
- **script/inference-clean-up.sh**: Cleaning up of what was executed with the worflow script.

First, it build the PV and PVC:

```bash
$ kubectl apply -f 01-dh-pv-nfs.yaml 

$ kubectl get pv && echo && kubectl get pvc 
```

and then, the PODs associated with model training. In case you already have a model, skip this step.

```bash
$ kubectl apply -f 02-dh-pod-training.yaml

$ kubectl get pod dh-pod-training 
```

When the POD of the training is completed, we deploy the inference. To do this, you use the worflow script.

```bash
$ cd script
$ ./inference-workflow.sh
```
Once everything is finished, to erase what was built with the worflow script, we launch the other script:

```bash
$ ./inference-clean-up.sh
```