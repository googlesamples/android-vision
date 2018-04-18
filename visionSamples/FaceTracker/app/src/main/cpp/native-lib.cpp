#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>

#include <dlib/dnn.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/svm/svm_multiclass_linear_trainer.h>

using namespace std;
using namespace dlib;

#define  LOG_TAG    "Native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct
{
    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
} argb;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_google_android_gms_samples_vision_face_facetracker_CustomDetector_test(JNIEnv *env,
                                                                                jobject instance,
                                                                                jobject bmp) {
    AndroidBitmapInfo infocolor;
    void* pixelscolor;
    int y;
    int x;
    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bmp, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return env->NewStringUTF("Image broken");
    }
    LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d",
         infocolor.width,infocolor.height,infocolor.stride,infocolor.format,infocolor.flags);

    if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return env->NewStringUTF("Image broken 2");
    }

    if ((ret = AndroidBitmap_lockPixels(env, bmp, &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    for (y=0;y<infocolor.height;y++) {
        argb * line = (argb *) pixelscolor;
        pixelscolor = (char *)pixelscolor + infocolor.stride;
    }

//    array2d<rgb_pixel> img;
//    img.set_size(infocolor.height, infocolor.width);
//    for (int y = 0; y < infocolor.height; y++){
//        for (int x = 0; x < infocolor.width; x++){
//            img[y][x] = pixelscolor[y * infocolor.width + x];
//        }
//    }


    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bmp);



    const char *returnValue = "test";
    return env->NewStringUTF(returnValue);
}