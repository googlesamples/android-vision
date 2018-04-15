#include <jni.h>
#include <string>
#include <android/log.h>

using namespace std;
#define AppTag "Native"

extern "C"
JNIEXPORT void JNICALL
Java_com_google_android_gms_samples_vision_face_facetracker_FaceGraphic_test(JNIEnv *env,
                                                                              jobject instance) {

    __android_log_print(ANDROID_LOG_DEBUG, AppTag, "Load resources started test1" );

}