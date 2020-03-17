# Test to adapted the EDDLL & ECVL libraries to Private Cloud

We tested the libraries in the DeepHealth's Kubernetes cluster and verify the correct functioning within the infrastructure. POD is building based on an examples proposed in the **Use Case Pipeline** repository.

## Objectives

- Check if it is possible to start two use-case examples within a DeepHealth cluster.

## Before you begin

- Dataset used: it's used [ISIC/Segmentation dataset](https://github.com/deephealthproject/use_case_pipeline/blob/master/README.md)

- You need to either have a dynamic Persistent Volume (PV) provisioner with a default StorageClass, or statically provision Persistent Volumes yourself to satisfy the Persistent Volume Claim (PVC) used here.

- **NFS storage** (it comes standard in DeepHealth's cluster). NFS server we can share folders over the network and allowed clients or system can access those shared folders and can use them in their applications.

## Configure a Pod to use a PV for NFS Storage

First, it build the PV and PVC:

**01-dh-pv.yaml**
```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: dh-pv
  labels:
    id: dh-nfs-pv
spec:
  storageClassName: storage-nfs
  capacity:
    storage: 20Gi
  accessModes:
    - ReadWriteMany
  nfs:
    server: deephealth1
    path: "/deephealth"
---
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: dh-pv-claim
spec:
  storageClassName: storage-nfs
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 20Gi
  selector:
    matchLabels:
      id: dh-nfs-pv
```

and finally, the PODs associated with model training and inference:

**02-dh-pod-training.yaml**
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: dh-pod-training
spec:
    volumes:
      - name: dh-volume
        persistentVolumeClaim:
          claimName: dh-pv-claim
    containers:
    - name: dhealth-pylibs
      image: dhealth/pylibs:latest
      volumeMounts:
        - name: dh-volume
          mountPath: /deephealth
      command: ["/bin/bash", "-c", "export OUTPUT_DIR=/deephealth/outputs; mkdir $OUTPUT_DIR/trash; python3 /deephealth/examples/use_case_pipeline/skin_lesion_segmentation_training.py /deephealth/dataset/isic_segmentation/isic_segmentation.yml --out-dir $OUTPUT_DIR --epochs 1 --batch-size 2"]
      resources:
        limits:
          memory: "3072Mi"
          cpu: "3000m"
    restartPolicy: Never
```

**03-dh-pod-inference.yaml**
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: dh-pod-inference
spec:
  volumes:
  - name: dh-volume
    persistentVolumeClaim:
      claimName: dh-pv-claim
  containers:
  - name: dhealth-pylibs
    image: dhealth/pylibs:latest
    volumeMounts:
    - name: dh-volume
      mountPath: /deephealth
    command: ["/bin/bash", "-c", "export OUTPUT_DIR=/deephealth/outputs; mkdir $OUTPUT_DIR/trash; python3 /deephealth/examples/use_case_pipeline/skin_lesion_segmentation_inference.py /deephealth/dataset/isic_segmentation/isic_segmentation.yml /deephealth/examples/use_case_pipeline/isic_segm_checkpoint.bin --out-dir $OUTPUT_DIR --batch-size 2"]
    resources:
      limits:
        memory: "3072Mi"
        cpu: "3000m"
  restartPolicy: Never
```
Initially we deployed the StorageClass and the PV/PVC:

```bash
$ kubectl apply -f 00-dh-sc.yaml && kubectl apply -f 01-dh-pv.yaml 

$ kubectl get sc && echo && kubectl get pv && echo && kubectl get pvc 
```

Then we run the training POD:

```bash
$ kubectl apply -f 02-dh-pod-training.yaml

$ kubectl get pod dh-pod-training 
```

At this point, we need to have the following resources deployed:

```bash
NAME                 PROVISIONER                AGE
standard (default)   k8s.io/dh-hostpath   60m
 
NAME                   CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                    STORAGECLASS   REASON   AGE
dh-pv-volume           20Gi       RWO            Retain           Bound    default/dh-pv-claim      nfs-storage             2m42s
 
NAME                  STATUS   VOLUME                 CAPACITY   ACCESS MODES   STORAGECLASS    AGE
dh-pv-claim           Bound    dh-pv-volume           20Gi       RWO            nfs-storage     2m42s
 
NAME             READY   STATUS      RESTARTS   AGE
dh-pod-training   0/1    Completed   0          2m37s
```

The output of training model is the following:

```bash

...
Epoch 1/1 (batch 1000/1000) - Batch 999 activation26(cross_entropy=15185.451,mean_squared_error=0.130)
Epoch 1/1 - Evaluation
Epoch 1/1 (batch 1/75) - IoU: 0.148217 - IoU: 0.163842
Epoch 1/1 (batch 2/75) - IoU: 0.500827 - IoU: 0.118781
...
Epoch 1/1 (batch 70/75) - IoU: 0.530869 - IoU: 0.136603
Epoch 1/1 (batch 71/75) - IoU: 0.454752 - IoU: 0.153192
Epoch 1/1 (batch 72/75) - IoU: 0.66641 - IoU: 0.368638
Epoch 1/1 (batch 73/75) - IoU: 0.192579 - IoU: 0.291509
Epoch 1/1 (batch 74/75) - IoU: 0.179956 - IoU: 0.647544
Epoch 1/1 (batch 75/75) - IoU: 0.279022 - IoU: 0.495301
MIoU: 0.344848
```
When the POD of the training is completed, we deploy the inference:

```bash
$ kubectl apply -f 03-dh-pod-inference.yaml

$ kubectl get pod dh-pod-inference
```

The output that this example offers us is the following:
```bash
Reading dataset
Testing
Batch 1/300 - IoU: 8.41043e-10 - IoU: 1.04844e-10
Batch 2/300 - IoU: 4.18585e-11 - IoU: 4.74811e-11
Batch 3/300 - IoU: 2.45399e-10 - IoU: 2.33155e-10
Batch 4/300 - IoU: 1.21595e-10 - IoU: 2.23464e-10
Batch 5/300 - IoU: 7.6864e-10 - IoU: 5.85138e-10
Batch 6/300 - IoU: 8.85191e-11 - IoU: 8.18532e-11
Batch 7/300 - IoU: 3.23625e-09 - IoU: 6.26606e-11
Batch 8/300 - IoU: 9.75515e-11 - IoU: 6.62691e-10
Batch 9/300 - IoU: 3.21027e-10 - IoU: 6.97837e-10
Batch 10/300 - IoU: 1.36166e-10 - IoU: 2.59403e-10
Batch 11/300 - IoU: 3.63636e-10 - IoU: 1.44155e-10
Batch 12/300 - IoU: 1.01678e-10 - IoU: 3.73274e-10
Batch 13/300 - IoU: 3.40356e-11 - IoU: 4.01526e-11
Batch 14/300 - IoU: 4.94805e-10 - IoU: 9.61816e-11
...
MIoU: 4.11689e-10
Generating Random Table
---------------------------------------------
input1         |  (3, 192, 192)=>      (3, 192, 192)
conv1          |  (3, 192, 192)=>      (64, 192, 192)
activation1    |  (64, 192, 192)=>      (64, 192, 192)
conv2          |  (64, 192, 192)=>      (64, 192, 192)
activation2    |  (64, 192, 192)=>      (64, 192, 192)
pool1          |  (64, 192, 192)=>      (64, 96, 96)
conv3          |  (64, 96, 96)=>      (128, 96, 96)
activation3    |  (128, 96, 96)=>      (128, 96, 96)
conv4          |  (128, 96, 96)=>      (128, 96, 96)
activation4    |  (128, 96, 96)=>      (128, 96, 96)
pool2          |  (128, 96, 96)=>      (128, 48, 48)
conv5          |  (128, 48, 48)=>      (256, 48, 48)
activation5    |  (256, 48, 48)=>      (256, 48, 48)
conv6          |  (256, 48, 48)=>      (256, 48, 48)
activation6    |  (256, 48, 48)=>      (256, 48, 48)
conv7          |  (256, 48, 48)=>      (256, 48, 48)
activation7    |  (256, 48, 48)=>      (256, 48, 48)
pool3          |  (256, 48, 48)=>      (256, 24, 24)
conv8          |  (256, 24, 24)=>      (512, 24, 24)
activation8    |  (512, 24, 24)=>      (512, 24, 24)
conv9          |  (512, 24, 24)=>      (512, 24, 24)
activation9    |  (512, 24, 24)=>      (512, 24, 24)
conv10         |  (512, 24, 24)=>      (512, 24, 24)
activation10   |  (512, 24, 24)=>      (512, 24, 24)
pool4          |  (512, 24, 24)=>      (512, 12, 12)
conv11         |  (512, 12, 12)=>      (512, 12, 12)
activation11   |  (512, 12, 12)=>      (512, 12, 12)
conv12         |  (512, 12, 12)=>      (512, 12, 12)
activation12   |  (512, 12, 12)=>      (512, 12, 12)
conv13         |  (512, 12, 12)=>      (512, 12, 12)
activation13   |  (512, 12, 12)=>      (512, 12, 12)
pool5          |  (512, 12, 12)=>      (512, 6, 6)
upsampling1    |  (512, 6, 6)=>      (512, 12, 12)
conv14         |  (512, 12, 12)=>      (512, 12, 12)
activation14   |  (512, 12, 12)=>      (512, 12, 12)
conv15         |  (512, 12, 12)=>      (512, 12, 12)
activation15   |  (512, 12, 12)=>      (512, 12, 12)
conv16         |  (512, 12, 12)=>      (512, 12, 12)
activation16   |  (512, 12, 12)=>      (512, 12, 12)
upsampling2    |  (512, 12, 12)=>      (512, 24, 24)
conv17         |  (512, 24, 24)=>      (512, 24, 24)
activation17   |  (512, 24, 24)=>      (512, 24, 24)
conv18         |  (512, 24, 24)=>      (512, 24, 24)
activation18   |  (512, 24, 24)=>      (512, 24, 24)
conv19         |  (512, 24, 24)=>      (256, 24, 24)
activation19   |  (256, 24, 24)=>      (256, 24, 24)
upsampling3    |  (256, 24, 24)=>      (256, 48, 48)
conv20         |  (256, 48, 48)=>      (256, 48, 48)
activation20   |  (256, 48, 48)=>      (256, 48, 48)
conv21         |  (256, 48, 48)=>      (256, 48, 48)
activation21   |  (256, 48, 48)=>      (256, 48, 48)
conv22         |  (256, 48, 48)=>      (128, 48, 48)
activation22   |  (128, 48, 48)=>      (128, 48, 48)
upsampling4    |  (128, 48, 48)=>      (128, 96, 96)
conv23         |  (128, 96, 96)=>      (128, 96, 96)
activation23   |  (128, 96, 96)=>      (128, 96, 96)
conv24         |  (128, 96, 96)=>      (64, 96, 96)
activation24   |  (64, 96, 96)=>      (64, 96, 96)
upsampling5    |  (64, 96, 96)=>      (64, 192, 192)
conv25         |  (64, 192, 192)=>      (64, 192, 192)
activation25   |  (64, 192, 192)=>      (64, 192, 192)
conv26         |  (64, 192, 192)=>      (1, 192, 192)
activation26   |  (1, 192, 192)=>      (1, 192, 192)
---------------------------------------------
```
