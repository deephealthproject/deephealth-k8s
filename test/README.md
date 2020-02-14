# Test to adapted the EDDLL & ECVL libraries to Private Cloud

We tested the libraries in the DeepHealth's Kubernetes cluster and verify the correct functioning within the infrastructure. POD is building based on an examples proposed in the **Use Case Pipeline** repository.

## Objectives

- Check if it is possible to start two use-case examples within a DeepHealth cluster.

## Before you begin

- Dataset used: it's used [ISIC/Segmentation dataset](https://github.com/deephealthproject/use_case_pipeline/blob/master/README.md)

- You need to either have a dynamic Persistent Volume (PV) provisioner with a default StorageClass, or statically provision Persistent Volumes yourself to satisfy the Persistent Volume Claim (PVC) used here.

## Configure a Pod to use a PV for Local Storage

First, create a file with Local Storage: 

**dh-sc.yaml**
```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: local-storage
provisioner: k8s.io/dh-hostpath
volumeBindingMode: WaitForFirstConsumer
```

Then I build the PV and PVC:

**dh-pv.yaml**
```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: deephealth-pv-volume
  labels:
    id: dh-vol
spec:
  storageClassName: standard
  capacity:
    storage: 20Gi
  accessModes:
    - ReadWriteOnce
  hostPath:
    path: "/deephealth"
---
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: deephealth-pv-claim
spec:
  storageClassName: standard
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 20Gi
  selector:
    matchLabels:
      id: dh-vol
```

y por último, el POD:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: deephealth-pod
spec:
    volumes:
      - name: dh-volume
        persistentVolumeClaim:
          claimName: deephealth-pv-claim
    containers:
    - name: dhealth-pylibs
      image: dhealth/pylibs:latest
      volumeMounts:
        - name: dh-volume
          mountPath: /deephealth
      command: ["/bin/bash", "-c",
      "cd /deephealth/examples/use_case_pipeline && python3 skin_lesion_segmentation.py '../../datasets/isic_segmentation/isic_segmentation.yml' --out-dir '../../outputs' --epochs 1 --batch-size 2 && sleep 5"
      ]
      resources:
        limits:
          memory: "2048Mi"
          cpu: "2000m"
    restartPolicy: Never
```

We run *kubectl-apply* to generate the volumes and the POD:

```bash
$ kubectl apply -f dh-sc.yaml && kubectl apply -f dh-pv.yaml && kubectl apply -f dh-pod.yaml

$ kubectl get sc && echo && kubectl get pv && echo && kubectl get pvc && echo && kubectl get pods
```

The last command has to return something similar to

```bash
NAME                 PROVISIONER                AGE
standard (default)   k8s.io/dh-hostpath   60m
 
NAME                   CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                         STORAGECLASS   REASON   AGE
deephealth-pv-volume   20Gi       RWO            Retain           Bound    default/deephealth-pv-claim   standard                2m42s
 
NAME                  STATUS   VOLUME                 CAPACITY   ACCESS MODES   STORAGECLASS   AGE
deephealth-pv-claim   Bound    deephealth-pv-volume   20Gi       RWO            standard       2m42s
 
NAME             READY   STATUS      RESTARTS   AGE
deephealth-pod   0/1     Completed   0          2m37s
```

The output that this example offers us is the following:
```bash
Reading dataset
Starting training
Epoch 1/1 (batch 1/1000) - Generating Random Table
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
 
Batch 0 activation26(cross_entropy=25773.643,mean_squared_error=0.252)
Epoch 1/1 (batch 2/1000) - Batch 1 activation26(cross_entropy=26388.342,mean_squared_error=0.261)
Epoch 1/1 (batch 3/1000) - Batch 2 activation26(cross_entropy=26337.727,mean_squared_error=0.260)
Epoch 1/1 (batch 4/1000) - Batch 3 activation26(cross_entropy=26229.393,mean_squared_error=0.259)
Epoch 1/1 (batch 5/1000) - Batch 4 activation26(cross_entropy=26088.959,mean_squared_error=0.257)
...
```

## Customization example

Following the example above, a file has been constructed based on the 'DeepHealth Toolkit Dataset Format', in order to perform a faster execution of the use case:

**isic_segmentation_small.yml**
```yaml
name: ISIC
description: ISIC segmentation dataset
images:
  - location: images_segmentation/ISIC_0014480.jpg
    label: ground_truth/ISIC_0014480_segmentation.png
  - location: images_segmentation/ISIC_0014569.jpg
    label: ground_truth/ISIC_0014569_segmentation.png
  - location: images_segmentation/ISIC_0000129.jpg
    label: ground_truth/ISIC_0000129_segmentation.png
  - location: images_segmentation/ISIC_0011157.jpg
    label: ground_truth/ISIC_0011157_segmentation.png
  - location: images_segmentation/ISIC_0014754.jpg
    label: ground_truth/ISIC_0014754_segmentation.png
  - location: images_segmentation/ISIC_0013499.jpg
    label: ground_truth/ISIC_0013499_segmentation.png
  - location: images_segmentation/ISIC_0000204.jpg
    label: ground_truth/ISIC_0000204_segmentation.png
  - location: images_segmentation/ISIC_0012303.jpg
    label: ground_truth/ISIC_0012303_segmentation.png
  - location: images_segmentation/ISIC_0010192.jpg
    label: ground_truth/ISIC_0010192_segmentation.png
  - location: images_segmentation/ISIC_0014433.jpg
    label: ground_truth/ISIC_0014433_segmentation.png
split:
  training:
    - 0
    - 1
    - 2
    - 3
    - 4
    - 5
  validation:
    - 6
    - 7
  test:
    - 8
    - 9
```

The output at the end of the execution is as follows:

```bash
Reading dataset
Starting training
Epoch 1/1 (batch 1/3) - Generating Random Table
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
 
Batch 0 activation26(cross_entropy=26009.758,mean_squared_error=0.256)
Epoch 1/1 (batch 2/3) - Batch 1 activation26(cross_entropy=25922.824,mean_squared_error=0.255)
Epoch 1/1 (batch 3/3) - Batch 2 activation26(cross_entropy=25822.771,mean_squared_error=0.253)
Validation - Epoch 1/1 (batch 1/1) - IoU: 0.00463517 - IoU: 3.87747e-10
MIoU: 0.00231758
```