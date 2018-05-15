package dlib.android;

public class FaceRecognizer {

    FaceRecognizer() {
        System.loadLibrary("native-lib");
    }

    String Recognize() {

        return recognizeNative();
    }
    public native String recognizeNative();
}
