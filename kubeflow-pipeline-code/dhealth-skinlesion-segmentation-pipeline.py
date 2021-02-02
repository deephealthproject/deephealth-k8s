#!/usr/bin/env python
# -*- coding: utf-8 -*-

import kfp
from kfp import dsl, compiler
from kfp.components import func_to_container_op, OutputPath
from kfp.dsl import ContainerOp
from kfp.dsl.types import Integer


# ========================= PYTHON's FUNCTIONS =========================
def _get_latest_model(model_file_folder, lmodel_path: OutputPath(str)):
    """
    Write the latest model in OutputPath
    """
    import os

    model_file = [
        model_file_folder + '/' + file
        for file in os.listdir(model_file_folder)
        if file.endswith('.bin')
    ]

    lasted = max(model_file, key=os.path.getctime)

    with open(lmodel_path, 'w') as f:
        f.write(lasted)


def _get_subyamls(output_dataset_folder: str, yamlfile_path: OutputPath(list)):
    """
    Write the list of sub-yamls in OutputPath
    """
    import os
    import json

    with open(yamlfile_path, 'w') as f:
        json.dump([
            output_dataset_folder + '/' + file
            for file in os.listdir(output_dataset_folder)
            if 'part-' in file and (file.endswith('part-$.yaml') or file.endswith('.yml'))
        ], f)

def _get_gpu(is_used: str, gpu_path: OutputPath(str)):
    import os 
    import json

    print("is_used: {}".format(is_used))
    with open(gpu_path, 'w') as f:
        if is_used == 'yes':
            f.write('yes')
        else:
            f.write('no')
        
    

# ========================= LIGHTWEIGHT PYTHON COMPONENTS =========================

_get_latest_model_op = func_to_container_op(_get_latest_model)

_get_yaml_op = func_to_container_op(_get_subyamls)

_get_gpu_op = func_to_container_op(_get_gpu)

# ========================= OPERATORS =========================

def dhealth_train_sl_segmentation_op(
        python_train_path,
        input_dataset_yaml,
        output_path,
        num_epochs: Integer(),
        num_batch_size: Integer(),
        gpu_boolean
):
    print("GPU: {}".format(gpu_boolean))
    if gpu_boolean == 'yes':
        return dsl.ContainerOp(
            name='DeepHealth - Train Skin Lesion Segmentation',
            image='dhealth/pylibs:latest',
            command=["python3", python_train_path],
            arguments=[
                input_dataset_yaml,
                '--out-dir', output_path,
                '--epochs', num_epochs,
                '--batch-size', num_batch_size,
                '--gpu'
            ]
        ).set_gpu_limit(1)
    else:
        return dsl.ContainerOp(
            name='DeepHealth - Train Skin Lesion Segmentation',
            image='dhealth/pylibs:latest',
            command=["python3", python_train_path],
            arguments=[
                input_dataset_yaml,
                '--out-dir', output_path,
                '--epochs', num_epochs,
                '--batch-size', num_batch_size,
            ]
        )


def dhealth_splityaml_op(input_dataset_yaml, output_dataset_folder, partition_number):
    return dsl.ContainerOp(
        name='DeepHealth - Split Dataset YAML',
        image='dhealth/split-yaml:latest',
        arguments=[
            input_dataset_yaml,
            partition_number,
            output_dataset_folder
        ]
    )


def dhealth_inference_sl_segmentation_op(
        python_train_path,
        input_dataset_yaml,
        model,
        output_path,
        num_batch_size: Integer(),
        gpu_boolean
):
    if gpu_boolean == 'yes':
        return dsl.ContainerOp(
            name='DeepHealth - Inference Skin Lesion Segmentation',
            image='dhealth/pylibs:latest',
            command=["python3", python_train_path],
            arguments=[
                input_dataset_yaml,
                model,
                '--out-dir', output_path,
                '--batch-size', num_batch_size,
                '--gpu'
            ]
        ).set_gpu_limit(1)
    else:
        return dsl.ContainerOp(
            name='DeepHealth - Inference Skin Lesion Segmentation',
            image='dhealth/pylibs:latest',
            command=["python3", python_train_path],
            arguments=[
                input_dataset_yaml,
                model,
                '--out-dir', output_path,
                '--batch-size', num_batch_size
            ]
        )


# ========================= PIPELINE =========================

@dsl.pipeline(
    name='Skin Lesion Segmentation',
    description='A trainer that does end-to-end for DeepHealth\'s use case. Addition, to distributed inference process'
)
def sl_segmentation_pipeline(
        python_train_path='/deephealth/use_case_pipeline/python/skin_lesion_segmentation_training.py',
        input_dataset_yaml='/deephealth/dataset/isic_segmentation/isic_segmentation.yml',
        output_path='/deephealth/outputs',
        num_epochs: Integer() = 1,
        num_batch_size: Integer() = 10,
        output_dataset_folder='/deephealth/dataset/isic_segmentation',
        split_partition_number: Integer() = 3,
        python_inference_path='/deephealth/use_case_pipeline/python/skin_lesion_segmentation_inference.py',
        model_file_folder='/deephealth/outputs',
        is_gpu_used='no'
):
    dhealth_vop = dsl.PipelineVolume(pvc='dhealth-efs-claim')
    dhealth_vop_param = {'/deephealth': dhealth_vop}

    # gpu = ''
    # if is_gpu_used == 'yes':
    #     gpu = 'yes'

    _gpu = _get_gpu_op(is_gpu_used) \
        .set_display_name("GPU input parameter") 

    _train_op = dhealth_train_sl_segmentation_op(python_train_path, input_dataset_yaml, output_path, num_epochs,
                                                 num_batch_size, _gpu.outputs) \
        .after(_gpu) \
        .add_pvolumes(dhealth_vop_param) \
        .set_display_name('Training Model')

    _split_yaml_op = dhealth_splityaml_op(input_dataset_yaml, output_dataset_folder, split_partition_number) \
        .after(_train_op) \
        .add_pvolumes(dhealth_vop_param) \
        .set_display_name('Split Datset YAML')

    model: ContainerOp = _get_latest_model_op(model_file_folder) \
        .after(_split_yaml_op) \
        .add_pvolumes({"/deephealth": dhealth_vop}) \
        .set_display_name('Load Model')

    subyamls = _get_yaml_op(output_dataset_folder) \
        .after(_split_yaml_op) \
        .add_pvolumes(dhealth_vop_param) \
        .set_display_name('Load sub-YAMLs')

    with dsl.ParallelFor(subyamls.outputs['yamlfile']) as sub_yaml:
        dhealth_inference_sl_segmentation_op(
            python_inference_path, sub_yaml, model.output, output_path, 2, is_gpu_used) \
            .add_pvolumes(dhealth_vop_param) \
            .set_display_name('Inference')


if __name__ == '__main__':
    kfp.compiler.Compiler().compile(sl_segmentation_pipeline, __file__ + '.yaml')
