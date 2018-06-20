[![Build Status](https://travis-ci.com/hoopoe/FR-android-dlib-opencv.svg?branch=master)](https://travis-ci.com/hoopoe/FR-android-dlib-opencv)

Facial Recognition on Android using dlib and opencv
============


This app demonstrate semi realtime face detection, tracking and recognition based on predefined face vectors.


Notes:
1. Set env variable OPENCV_ANDROID_SDK 

export OPENCV_ANDROID_SDK=/Users/hoopoe/Tools/OpenCV-android-sdk

2. From https://github.com/davisking/dlib-models

  copy 
  * shape_predictor_5_face_landmarks.dat
  * dlib_face_recognition_resnet_model_v1.dat
  
  to /sdcard/Download 

3. dlib adapted to work with -DANDROID_STL=gnustl_shared


4. To enable Mobilenet/TF activity. You need to:

  Copy some model from 
  https://github.com/tensorflow/models/blob/master/research/slim/nets/mobilenet_v1.md
  
  Change some values manually 

  private static final int MAX_RESULTS = 500;

  private static final int TF_OD_API_INPUT_SIZE = 416;
  
  private static final String TF_OD_API_MODEL_FILE ="file:///android_asset/spc_mobilenet_v3_1x_0.52_cleaned.pb"

5. OpenCV disabled for now

