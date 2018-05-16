package dlib.android;

import android.graphics.Bitmap;

public class FaceRecognizer {

    public FaceRecognizer() { }

    public void loadNative()
    {
        System.loadLibrary("native-lib");
        loadResourcesPart1();
        loadResourcesPart2();
    }

    private native int loadResourcesPart1();
    private native int loadResourcesPart2();
    public native String recognizeNative1(Bitmap bmp); //full image screen
    public native String recognizeNative2(Bitmap bmp); //customDetector
}
