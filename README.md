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
