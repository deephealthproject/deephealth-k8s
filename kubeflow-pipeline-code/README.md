# Kubeflow Pipeline

We tested the use case from ECVL libraries in the Kubeflow Pipeline. The goal is to launch in a distributed/parallel way the inference.

After defining the pipeline in Python as described above, you must compile the pipeline to an intermediate representation before you can submit it to the Kubeflow Pipelines service. 

```bash
$ dsl-compile --py [path/to/python/file.py] --output [path/to/output/file.yaml]
```