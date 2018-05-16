package dlib.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class FaceRecognizerTest {
    private static final String TAG = "FaceRecognizerTest";

    @Test
    public void RecognizeTest() throws IOException {
        FaceRecognizer fr = new FaceRecognizer();
        fr.loadNative();
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        InputStream testInput = testContext.getAssets().open("2.bmp");
        Bitmap bitmap = BitmapFactory.decodeStream(testInput);
        Log.i(TAG, String.format("test1: %d", bitmap.getAllocationByteCount()));
        String res = fr.recognizeNative1(bitmap);
        assertEquals("Unknown", res);
    }
}
