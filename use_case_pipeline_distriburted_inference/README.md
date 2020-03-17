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