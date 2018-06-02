#include <jni.h>
#include <string>
#include <dirent.h>
#include <unordered_map>
#include <android/log.h>
#include <android/bitmap.h>
#include <dlib/image_io.h>

#include <dlib/dnn.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/svm/svm_multiclass_linear_trainer.h>

using namespace std;
using namespace dlib;

#define  LOG_TAG    "Native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


template <template <int,template<typename>class,int,typename> class block, int N, template<typename>class BN, typename SUBNET>
using residual = dlib::add_prev1<block<N,BN,1,dlib::tag1<SUBNET>>>;

template <template <int,template<typename>class,int,typename> class block, int N, template<typename>class BN, typename SUBNET>
using residual_down = dlib::add_prev2<dlib::avg_pool<2,2,2,2,dlib::skip1<dlib::tag2<block<N,BN,2,dlib::tag1<SUBNET>>>>>>;

template <int N, template <typename> class BN, int stride, typename SUBNET>
using block  = BN<dlib::con<N,3,3,1,1,dlib::relu<BN<dlib::con<N,3,3,stride,stride,SUBNET>>>>>;

template <int N, typename SUBNET> using ares      = dlib::relu<residual<block,N,dlib::affine,SUBNET>>;
template <int N, typename SUBNET> using ares_down = dlib::relu<residual_down<block,N,dlib::affine,SUBNET>>;

template <typename SUBNET> using alevel0 = ares_down<256,SUBNET>;
template <typename SUBNET> using alevel1 = ares<256,ares<256,ares_down<256,SUBNET>>>;
template <typename SUBNET> using alevel2 = ares<128,ares<128,ares_down<128,SUBNET>>>;
template <typename SUBNET> using alevel3 = ares<64,ares<64,ares<64,ares_down<64,SUBNET>>>>;
template <typename SUBNET> using alevel4 = ares<32,ares<32,ares<32,SUBNET>>>;

using anet_type = dlib::loss_metric<dlib::fc_no_bias<128, dlib::avg_pool_everything<
        alevel0<
                alevel1<
                        alevel2<
                                alevel3<
                                        alevel4<
                                                dlib::max_pool<3,3,2,2,dlib::relu<dlib::affine<dlib::con<32,7,7,2,2,
                                                        dlib::input_rgb_image_sized<150>
                                                >>>>>>>>>>>>;

dlib::frontal_face_detector detector = dlib::get_frontal_face_detector();
dlib::frontal_face_detector detector1 = dlib::get_frontal_face_detector();
dlib::shape_predictor sp, sp1;
anet_type net, net1;

//std::vector<matrix<float, 0, 1>> known_faces;

std::unordered_map<std::string, matrix<float, 0, 1>> known_faces;

typedef matrix<float, 0, 1> sample_type;
typedef linear_kernel<sample_type> lin_kernel;
multiclass_linear_decision_function<lin_kernel, string> df;

typedef struct
{
    uint8_t alpha;//r
    uint8_t red;//g
    uint8_t green;//b
    uint8_t blue;//a  see rgb_pixel assignment
} argb;

float FACE_RECOGNIZE_THRESH = 0.55;

extern "C"
JNIEXPORT jint JNICALL
Java_com_google_android_gms_samples_vision_face_facetracker_FaceTrackerActivity_loadResources(
        JNIEnv *env, jobject instance)
{

    LOGI("load resource");
    FILE *file1 = fopen("/storage/emulated/0/Download/shape_predictor_5_face_landmarks.dat", "r+");
    FILE *file2 = fopen("/storage/emulated/0/Download/dlib_face_recognition_resnet_model_v1.dat",
                        "r+");
    //FILE *file3 = fopen("/storage/emulated/0/Download/faces_linear.svm", "r+");

    if (file1 != NULL && file2 != NULL ) {
        fclose(file1);
        fclose(file2);
        //fclose(file3);
        dlib::deserialize("/storage/emulated/0/Download/shape_predictor_5_face_landmarks.dat")
                >> sp;
        dlib::deserialize("/storage/emulated/0/Download/shape_predictor_5_face_landmarks.dat")
                >> sp1;
        dlib::deserialize("/storage/emulated/0/Download/dlib_face_recognition_resnet_model_v1.dat")
                >> net;
        dlib::deserialize("/storage/emulated/0/Download/dlib_face_recognition_resnet_model_v1.dat")
                >> net1;
        //dlib::deserialize("/storage/emulated/0/Download/faces_linear.svm") >> df;

        DIR *d;
        char *p1,*p2;
        int ret;
        struct dirent *dir;
        d = opendir("/storage/emulated/0/Download");
        if (d)
        {
            LOGI("Loading feature vectors using *.vec", p1);
            while ((dir = readdir(d)) != NULL)
            {
                p1=strtok(dir->d_name,".");
                p2=strtok(NULL,".");
                if(p2!=NULL)
                {
                    ret=strcmp(p2,"vec");
                    if(ret==0)
                    {
                        std::string name = std::string(p1);
                        std::string file = name + ".vec";
                        matrix<float, 0, 1> face_vector;
                        dlib::deserialize("/storage/emulated/0/Download/"  + file) >> face_vector;
                        known_faces.insert({name, face_vector});
                    }
                }

            }
            closedir(d);
        }
    } else {
        return 1; //failed
    }

    return 0;
}extern "C"
JNIEXPORT jint JNICALL
Java_dlib_android_FaceRecognizer_loadResourcesPart1(JNIEnv *env, jobject instance) {

    LOGI("load resource part1");
    FILE *file1 = fopen("/storage/emulated/0/Download/shape_predictor_5_face_landmarks.dat", "r+");
    FILE *file2 = fopen("/storage/emulated/0/Download/dlib_face_recognition_resnet_model_v1.dat",
                        "r+");

    if (file1 != NULL && file2 != NULL ) {
        fclose(file1);
        fclose(file2);
        dlib::deserialize("/storage/emulated/0/Download/shape_predictor_5_face_landmarks.dat")
                >> sp;
        dlib::deserialize("/storage/emulated/0/Download/dlib_face_recognition_resnet_model_v1.dat")
                >> net;

        DIR *d;
        char *p1,*p2;
        int ret;
        struct dirent *dir;
        d = opendir("/storage/emulated/0/Download");
        if (d)
        {
            LOGI("Loading feature vectors using *.vec", p1);
            while ((dir = readdir(d)) != NULL)
            {
                p1=strtok(dir->d_name,".");
                p2=strtok(NULL,".");
                if(p2!=NULL)
                {
                    ret=strcmp(p2,"vec");
                    if(ret==0)
                    {
                        std::string name = std::string(p1);
                        std::string file = name + ".vec";
                        matrix<float, 0, 1> face_vector;
                        dlib::deserialize("/storage/emulated/0/Download/"  + file) >> face_vector;
                        known_faces.insert({name, face_vector});
                    }
                }

            }
            closedir(d);
        }
    } else {
        return -1; //failed
    }

    return 0;

}extern "C"
JNIEXPORT jint JNICALL
Java_dlib_android_FaceRecognizer_loadResourcesPart2(JNIEnv *env, jobject instance) {

    LOGI("load resource part2");
    FILE *file1 = fopen("/storage/emulated/0/Download/shape_predictor_5_face_landmarks.dat", "r+");
    FILE *file2 = fopen("/storage/emulated/0/Download/dlib_face_recognition_resnet_model_v1.dat",
                        "r+");

    if (file1 != NULL && file2 != NULL ) {
        fclose(file1);
        fclose(file2);
        dlib::deserialize("/storage/emulated/0/Download/shape_predictor_5_face_landmarks.dat")
                >> sp1;
        dlib::deserialize("/storage/emulated/0/Download/dlib_face_recognition_resnet_model_v1.dat")
                >> net1;
    } else{
        return -1;
    }
    return 0;
}extern "C"
JNIEXPORT jobjectArray JNICALL
Java_dlib_android_FaceRecognizer_recognizeFaces(JNIEnv *env,
                                                         jobject instance,
                                                         jobject bmp) {
    jobjectArray strarr;
    std::vector<string> names;

    AndroidBitmapInfo infocolor;
    void *pixelscolor;
    int y;
    int x;
    int ret;
    array2d<rgb_pixel> img;
    if ((ret = AndroidBitmap_getInfo(env, bmp, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        //return env->NewStringUTF("Image broken");
        return strarr;
    }
    LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d",
         infocolor.width, infocolor.height, infocolor.stride, infocolor.format, infocolor.flags);

    if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        //return env->NewStringUTF("Image broken 2");
        return strarr;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bmp, &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    img.set_size(infocolor.height, infocolor.width);

    LOGI("size w=%d h=%d", infocolor.width, infocolor.height);
    for (y = 0; y < infocolor.height; y++) { //todo: performance
        argb *line = (argb *) pixelscolor;
        for (x = 0; x < infocolor.width; ++x) {
            rgb_pixel p(line[x].alpha, line[x].red, line[x].green);
            img[y][x] = p;
        }
        pixelscolor = (char *) pixelscolor + infocolor.stride;
    }

    //dlib::save_bmp(img, "/sdcard/Download/res1.bmp");

    std::vector<dlib::rectangle> dets = detector1(img);
    if (dets.size() == 0){
        return strarr;
    }

    std::vector<matrix<rgb_pixel>> faces;
    for (auto face : dets)
    {
        auto shape = sp1(img, face);
        matrix<rgb_pixel> face_chip;
        extract_image_chip(img, get_face_chip_details(shape, 150, 0.25), face_chip);
        faces.push_back(move(face_chip));
    }

    std::vector<matrix<float, 0, 1>> face_descriptors = net1(faces);

    for (size_t i = 0; i < face_descriptors.size(); ++i)
    {
        std::string name = "Unknown";
        for (auto j : known_faces) {
            float dist = length(face_descriptors[i] - j.second);
            if (dist < FACE_RECOGNIZE_THRESH) {
                name = j.first;
                break;
            }
        }
        names.push_back(name);
    }

    AndroidBitmap_unlockPixels(env, bmp);

    if(names.size() > 0) {
        strarr = env->NewObjectArray(names.size(), env->FindClass("java/lang/String"), nullptr);
        for (int i = 0; i < names.size(); ++i)
        {
            env->SetObjectArrayElement(strarr, i, env->NewStringUTF(names[i].c_str()));
        }
    }
    return strarr;
}extern "C"
JNIEXPORT jstring JNICALL
Java_dlib_android_FaceRecognizer_recognizeFace(JNIEnv *env, jobject instance, jobject bmp) {

    AndroidBitmapInfo infocolor;
    void *pixelscolor;
    int y;
    int x;
    int ret;
    array2d<rgb_pixel> img;
    if ((ret = AndroidBitmap_getInfo(env, bmp, &infocolor)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return env->NewStringUTF("Image broken");
    }
    LOGI("color image :: width is %d; height is %d; stride is %d; format is %d;flags is %d",
         infocolor.width, infocolor.height, infocolor.stride, infocolor.format, infocolor.flags);

    if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return env->NewStringUTF("Image broken 2");
    }

    if ((ret = AndroidBitmap_lockPixels(env, bmp, &pixelscolor)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }

    img.set_size(infocolor.height, infocolor.width);
    for (y = 0; y < infocolor.height; y++) { //todo: performance
        argb *line = (argb *) pixelscolor;
        for (x = 0; x < infocolor.width; ++x) {
            rgb_pixel p(line[x].alpha, line[x].red, line[x].green);
            img[y][x] = p;
        }
        pixelscolor = (char *) pixelscolor + infocolor.stride;
    }

    //todo: smth wrong with colors
    //dlib::save_bmp(img, "/sdcard/Download/res.bmp");

    std::vector<dlib::rectangle> dets = detector(img);
    LOGI("detected size %d", dets.size());

    float min_dist = 0.0;
    if(dets.size() > 0 ){
        auto face = dets.front();
        std::vector<matrix<rgb_pixel>> faces;
        int x = face.left();
        int y = face.top();
        int width = face.width();
        int height = face.height();

        auto shape = sp(img, face);
        LOGI("shape predictor");
        matrix<rgb_pixel> face_chip;
        extract_image_chip(img, get_face_chip_details(shape, 150, 0.25), face_chip);
        faces.push_back(move(face_chip));

        LOGI("before recognized size %d", 0);
        std::vector<matrix<float, 0, 1>> face_descriptors = net(faces);
        LOGI("after recognized size %d", face_descriptors.size());
        if (face_descriptors.size() > 0)
        {
            matrix<float, 0, 1> face_desc = face_descriptors[0];
            for (auto& i : known_faces) {
                float dist = length(face_desc -  i.second );
                if (dist < min_dist){
                    min_dist = dist;
                }
                if( dist < FACE_RECOGNIZE_THRESH)
                {
                    //LOGI("recognized");
                    return env->NewStringUTF(i.first.c_str());
                }
            }
        }
        LOGI("not recognized, max dist %0.2f", min_dist);
    }

//    LOGI("unlocking pixels");
    AndroidBitmap_unlockPixels(env, bmp);

    std::string returnValue = "Unknown";
    return env->NewStringUTF(returnValue.c_str());
}