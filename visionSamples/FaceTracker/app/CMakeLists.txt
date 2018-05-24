
# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set (CMAKE_CXX_STANDARD 11)


# OpenCV stuff
include_directories(/home/alorusso/Downloads/OpenCV-android-sdk/sdk/native/jni/include)
#add_library( lib_opencv SHARED IMPORTED )
#set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

# /home/alorusso/workspace/SmartGlasses/Development/ANDROID/Android-vision/visionSamples/FaceTracker/app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)
# ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

include_directories(/home/alorusso/Downloads/opencv-3.3.0/modules/core/include/)
set(OPENCV_DIR /home/alorusso/Downloads/opencv-3.3.0/modules)
include_directories(${OPENCV_DIR})

add_library( # Sets the name of the library.
             lib_opencv

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             /home/alorusso/Downloads/opencv-3.3.0/modules/objdetect/src/cascadedetect.cpp
             )



add_library( # Sets the name of the library.
             OCV-native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/OCV-native-lib.cpp )



target_link_libraries( # Specifies the target library.
                       OCV-native-lib

                       # OpenCV lib
                       lib_opencv

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )


set(DLIB_DIR ../../../dlib)
include_directories(${DLIB_DIR})

add_library( # Sets the name of the library.
             lib_dlib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             ../../../dlib/dlib/threads/threads_kernel_shared.cpp
             ../../../dlib/dlib/entropy_decoder/entropy_decoder_kernel_2.cpp
             ../../../dlib/dlib/base64/base64_kernel_1.cpp
             ../../../dlib/dlib/threads/threads_kernel_1.cpp
             ../../../dlib/dlib/threads/threads_kernel_2.cpp
             ../../../dlib/dlib/threads/thread_pool_extension.cpp
             ../../../dlib/dlib/threads/async.cpp
             ../../../dlib/dlib/dnn/cpu_dlib.cpp
             ../../../dlib/dlib/dnn/tensor_tools.cpp
             )

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/native-lib.cpp )




# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       lib_dlib

                       -ljnigraphics

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
