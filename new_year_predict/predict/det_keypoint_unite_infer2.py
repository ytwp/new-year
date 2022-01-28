# Copyright (c) 2021 PaddlePaddle Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import json
import time

import cv2
import math
import numpy as np
import paddle

from det_keypoint_unite_utils import argsparser
from preprocess import decode_image, decode_base64_image, decode_array_buffer_image
from infer import Detector, DetectorPicoDet, PredictConfig, print_arguments, get_test_images
from keypoint_infer import KeyPoint_Detector, PredictConfig_KeyPoint
from visualize import draw_pose
from benchmark_utils import PaddleInferBenchmark
from utils import get_current_memory_mb
from keypoint_postprocess import translate_to_ori_images

KEYPOINT_SUPPORT_MODELS = {
    'HigherHRNet': 'keypoint_bottomup',
    'HRNet': 'keypoint_topdown'
}
print("bei jia zai le")


# 用给定数据预测
def predict_with_given_det(image, det_res, keypoint_detector,
                           keypoint_batch_size, det_threshold,
                           keypoint_threshold, run_benchmark):
    rec_images, records, det_rects = keypoint_detector.get_person_from_rect(
        image, det_res, det_threshold)
    keypoint_vector = []
    score_vector = []
    rect_vector = det_rects
    batch_loop_cnt = math.ceil(float(len(rec_images)) / keypoint_batch_size)

    for i in range(batch_loop_cnt):
        start_index = i * keypoint_batch_size
        end_index = min((i + 1) * keypoint_batch_size, len(rec_images))
        batch_images = rec_images[start_index:end_index]
        batch_records = np.array(records[start_index:end_index])
        if run_benchmark:
            # warmup
            keypoint_result = keypoint_detector.predict(
                batch_images, keypoint_threshold, repeats=10, add_timer=False)
            # run benchmark
            keypoint_result = keypoint_detector.predict(
                batch_images, keypoint_threshold, repeats=10, add_timer=True)
        else:
            keypoint_result = keypoint_detector.predict(batch_images,
                                                        keypoint_threshold)
        orgkeypoints, scores = translate_to_ori_images(keypoint_result,
                                                       batch_records)
        keypoint_vector.append(orgkeypoints)
        score_vector.append(scores)

    keypoint_res = {}
    keypoint_res['keypoint'] = [
        np.vstack(keypoint_vector).tolist(), np.vstack(score_vector).tolist()
    ] if len(keypoint_vector) > 0 else [[], []]
    keypoint_res['bbox'] = rect_vector
    return keypoint_res


# 自上而下联合预测
def topdown_unite_predict(detector,
                          topdown_keypoint_detector,
                          image_list,
                          keypoint_batch_size=1,
                          save_res=False):
    det_timer = detector.get_timer()
    for i, img_file in enumerate(image_list):
        det_timer.preprocess_time_s.start()
        image, _ = img_file
        det_timer.preprocess_time_s.end()
        # 预测行人
        print("行人开始时间: {}".format(time.time()))
        results = detector.predict([image])

        print("行人结束时间: {}".format(time.time()))
        # 判断是否预测到行人
        if results['boxes_num'] == 0:
            continue
        # 预测关键点
        print("关键点开始时间: {}".format(time.time()))
        keypoint_res = predict_with_given_det(
            image, results, topdown_keypoint_detector, keypoint_batch_size, 0.5,
            0.5, False)
        print("关键点结束时间: {}".format(time.time()))
        # draw_pose(img_file, keypoint_res)
        skeletons, scores = keypoint_res['keypoint']
        skeletons = np.array(skeletons)
        kpt_nums = 17
        if len(skeletons) > 0:
            kpt_nums = skeletons.shape[1]
        if kpt_nums == 17:  # plot coco keypoint
            EDGES = [(0, 1), (0, 2), (1, 3), (2, 4), (3, 5), (4, 6), (5, 7), (6, 8),
                     (7, 9), (8, 10), (5, 11), (6, 12), (11, 13), (12, 14),
                     (13, 15), (14, 16), (11, 12)]
        else:  # plot mpii keypoint
            EDGES = [(0, 1), (1, 2), (3, 4), (4, 5), (2, 6), (3, 6), (6, 7), (7, 8),
                     (8, 9), (10, 11), (11, 12), (13, 14), (14, 15), (8, 12),
                     (8, 13)]
        NUM_EDGES = len(EDGES)
        # (int(np.mean(skeletons[j][i, 0])), int(np.mean(skeletons[j][i, 1])))
        res = []
        for j in range(len(skeletons)):
            res.append({})
        skeleton_index = {
            0: 'nose',
            1: 'left_eye',
            2: 'right_eye',
            3: 'left_ear',
            4: 'right_ear',
            5: 'left_shoulder',
            6: 'right_shoulder',
            7: 'left_elbow',
            8: 'right_elbow',
            9: 'left_wrist',
            10: 'right_wrist',
            11: 'left_crotch',
            12: 'right_crotch',
            13: 'left_knee',
            14: 'right_knee',
            15: 'left_ankle',
            16: 'right_ankle'
        }
        # i 是骨骼点 j是人
        for i in range(NUM_EDGES):
            for j in range(len(skeletons)):
                skeleton_res = res[j]
                skeleton_res[skeleton_index[i]] = {
                    'x': skeletons[j][i, 0],
                    'y': skeletons[j][i, 1]
                }
        return res


det_model_dir = 'output_inference/picodet_s_320_pedestrian'
keypoint_model_dir = 'output_inference/tinypose_128x96'
device = 'GPU'
# 行人检测模型
# 取行人检测模型路径
pred_config = PredictConfig(det_model_dir)
# 探测器类型：轻量级检测模型PicoDet
detector_func = 'Detector'
if pred_config.arch == 'PicoDet':
    detector_func = 'DetectorPicoDet'
# 加载模型
detector = eval(detector_func)(pred_config,
                               det_model_dir,
                               device=device)
# 关键点检测模型
# 取关键点检测模型路径
pred_config = PredictConfig_KeyPoint(keypoint_model_dir)
assert KEYPOINT_SUPPORT_MODELS[
           pred_config.
               arch] == 'keypoint_topdown', 'Detection-Keypoint unite inference only supports topdown models.'
# 加载关键点检测模型
topdown_keypoint_detector = KeyPoint_Detector(
    pred_config,
    keypoint_model_dir,
    device=device)


def main():
    img_list = ['input/111.jpg']
    # 自上而下联合预测
    keypoint_res = topdown_unite_predict(detector, topdown_keypoint_detector, img_list)
    return keypoint_res


def base64(base64_image):
    img_list = [decode_base64_image(base64_image, {})]
    # 自上而下联合预测
    keypoint_res = topdown_unite_predict(detector, topdown_keypoint_detector, img_list)
    return keypoint_res


def array_buffer(img_array, width, height):
    img_list = [decode_array_buffer_image(img_array, width, height, {})]
    # 自上而下联合预测
    keypoint_res = topdown_unite_predict(detector, topdown_keypoint_detector, img_list)
    return keypoint_res
