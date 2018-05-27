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

#define AppTag "OCV-FD::Activity"

extern "C"
{


void detect(Mat &gray) {

    String face_cascade_name = "/storage/emulated/0/Movies/haarcascade_frontalface_default.xml";

    CascadeClassifier face_cascade;
    std::vector<Rect> faces;

    if( !face_cascade.load( face_cascade_name ) ){
        printf("--(!)Error loading\n");
        __android_log_print(ANDROID_LOG_DEBUG, AppTag, "Resources NOT found: exiting");
        return;
    }

    face_cascade.detectMultiScale(gray,faces,1.1,3,0,Size(20,20),Size(1000,1000));

    for(size_t i=0; i<faces.size(); i++)
    {
        rectangle(gray,faces[i],cv::Scalar(255, 255, 255), 2, 8, 0);
    }
}


void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_MainActivity_imgProcess(JNIEnv *env, jclass,
                                                                    jlong inputAddrMat,
                                                                    jlong imageAddrGray) {
    Mat &mRgb = *(Mat *)inputAddrMat;
    Mat &mGray = *(Mat *)imageAddrGray;

    detect (mGray);
}

}
