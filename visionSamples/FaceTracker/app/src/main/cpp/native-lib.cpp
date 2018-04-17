#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>

using namespace std;

#define  LOG_TAG    "Native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_google_android_gms_samples_vision_face_facetracker_CustomDetector_test(JNIEnv *env,
                                                                                jobject instance,
                                                                                jobject bmp) {
    AndroidBitmapInfo infocolor;
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
    const char *returnValue = "test";
    return env->NewStringUTF(returnValue);
}