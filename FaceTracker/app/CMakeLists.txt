# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_VERBOSE_MAKEFILE on)
set (CMAKE_CXX_STANDARD 11)


# OpenCV stuff
#include_directories(/home/irina/caffe-android-lib/android_lib/opencv/sdk/native/jni/include)
#set(OpenCV_DIR "/home/irina/caffe-android-lib/android_lib/opencv/sdk/native/jni")
#find_package(OpenCV REQUIRED)
#include_directories(${OpenCV_INCLUDE_DIRS})

include_directories(/home/irina/caffe-android-lib/android_lib/opencv/sdk/native/jni/include)
add_library( lib_opencv SHARED IMPORTED )
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION /home/irina/caffe-android-lib/android_lib/opencv/sdk/native/libs/${ANDROID_ABI}/libopencv_java3.so)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

set(DLIB_DIR ../../dlib)
include_directories(${DLIB_DIR})
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

add_library( # Sets the name of the library.
             lib_dlib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             ../../dlib/dlib/threads/threads_kernel_shared.cpp
             ../../dlib/dlib/entropy_decoder/entropy_decoder_kernel_2.cpp
             ../../dlib/dlib/base64/base64_kernel_1.cpp
             ../../dlib/dlib/threads/threads_kernel_1.cpp
             ../../dlib/dlib/threads/threads_kernel_2.cpp
             ../../dlib/dlib/threads/thread_pool_extension.cpp
             ../../dlib/dlib/threads/async.cpp
             ../../dlib/dlib/dnn/cpu_dlib.cpp
             ../../dlib/dlib/dnn/tensor_tools.cpp
             )

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/native-lib.cpp )

target_link_libraries( # Specifies the target library.
                       native-lib

                       lib_dlib

                        -ljnigraphics

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )



add_library( # Sets the name of the library.
             OCV-native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/OCV-native-lib.cpp )

target_link_libraries(
                        OCV-native-lib

                       lib_opencv

                       ${log-lib} )

add_subdirectory("./jni")