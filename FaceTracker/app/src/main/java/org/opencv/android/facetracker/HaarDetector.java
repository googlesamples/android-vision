package org.opencv.android.facetracker;


/**
 * Created by alorusso on 06/06/18.
 */

public class HaarDetector {
    private static final String TAG = "OCV-HaarDetector";


    public HaarDetector() {
    }

    public void loadNative() {
        System.loadLibrary("OCV-native-lib");

        loadResources();
    }

    public void OCvDetect(long imageGray, long faces) {
        OpenCVdetector(imageGray, faces);
    }

    private native void OpenCVdetector(long imageGray, long faces);
    private native void loadResources();
}
