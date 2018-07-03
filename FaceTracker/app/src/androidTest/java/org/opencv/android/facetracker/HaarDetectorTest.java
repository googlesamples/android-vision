package org.opencv.android.facetracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

import java.io.IOException;
import java.io.InputStream;
import android.support.test.InstrumentationRegistry;
import org.junit.Test;


import static junit.framework.Assert.assertEquals;

/**
 * Created by alorusso on 07/06/18.
 */

public class HaarDetectorTest {
    private static final String TAG = "FaceTrackerTest";
    static {
        System.loadLibrary("opencv_java3");
    }

    public HaarDetectorTest() {
    }

    @Test
    public void OCVTrackerTest() throws IOException {

        HaarDetector hd = new HaarDetector();
        hd.loadNative();

        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        InputStream testInput = testContext.getAssets().open("11.png");
        Bitmap bitmap = BitmapFactory.decodeStream(testInput);

        Mat matImg = new Mat();
        MatOfRect rectList = new MatOfRect();

        Utils.bitmapToMat(bitmap, matImg);

        hd.OCvDetect(matImg.getNativeObjAddr(), rectList.getNativeObjAddr());

        System.out.println("Number of faces = " + rectList.size());

        //
        //assertEquals("Number of faces %d", rectList.size());
        //Rect[] faces = rects.toArray();
        //
        //for (int i=0; i<faces.length; i++)
        //Imgproc.rectangle(bitmap, faces[i].tl(),faces[i].br(), Scalar(255,255,255,255),3)
        //
    }
}
