#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

/* these seem not needed */
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/objdetect/detection_based_tracker.hpp>
#include <opencv2/features2d/features2d.hpp>

#include <android/log.h>

using namespace std;
using namespace cv;


#define  LOG_TAG    "OCV-Native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)



extern "C"
{

/*
inline void vector_Rect_to_Mat(vector<Rect>& v_rect, Mat& mat)
{
    mat = Mat(v_rect, true);
}
*/

inline void vector_Rect_to_Mat(std::vector<Rect>& v_rect, Mat& mat)
{
    mat = Mat(v_rect, true);
}


CascadeClassifier face_cascade;

vector<Rect> detect(Mat &gray) {

    std::vector<Rect> faces = {};
    face_cascade.detectMultiScale(gray, faces, 1.1, 3, 0, Size(20, 20), Size(1000, 1000));

    /*
    for (size_t i = 0; i < faces.size(); i++) {
        rectangle(gray, faces[i], cv::Scalar(255, 255, 255), 2, 8, 0);
    }
    */
    return faces;
}



JNIEXPORT void JNICALL
Java_org_opencv_android_facetracker_HaarDetector_loadResources(
        JNIEnv *env, jobject instance)
{
    String face_cascade_name = "/sdcard/Download/haarcascade_frontalface_default.xml";

    if (!face_cascade.load(face_cascade_name)) {
        LOGE("OCV resources NOT loaded");
        return;
    } else {
        LOGI("OCV resources loaded");
    }
}


JNIEXPORT void JNICALL
Java_org_opencv_android_facetracker_HaarDetector_OpenCVdetector(JNIEnv *env, jclass instance,
                                                                jlong inputAddrMat, jlong matRects) {

    vector<Rect> faces;


    Mat &origImg = *((Mat *)inputAddrMat);
    Mat mGray;
    cv::cvtColor(origImg, mGray, CV_BGR2GRAY);

    faces = detect (mGray);
    //faces = detect(origImg);

    vector_Rect_to_Mat(faces, *((Mat*)matRects));
}
}

// inline void Mat_to_vector_Rect (Mat &mat, vector <Rect> &v_rect);
// ((Mat*)matRect) = Mat(faces, true);