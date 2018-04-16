package com.google.android.gms.samples.vision.face.facetracker;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class CustomDetector extends Detector<Face> {
    private static final String TAG = "CustomDetector";
    private Detector<Face> mDelegate;
    private Map<Face, String> mFaceNameMap;

    CustomDetector(Detector<Face> delegate, Map<Face, String> faceNameMap) {
        mDelegate = delegate;
        mFaceNameMap = faceNameMap;
    }
    //@Override
    public SparseArray<Face> detect(Frame frame) {
        Log.i(TAG, "CustomDetector detect");
        SparseArray<Face> faces = mDelegate.detect(frame);
//        Bitmap bmp = frame.getBitmap();
//
//        FileOutputStream out = null;
//        try {
//            String path = Environment.getExternalStorageDirectory().toString();
//            out = new FileOutputStream(path +"SDCARD name + increment");
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (out != null) {
//                    out.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        for(int i = 0; i < faces.size(); i++) {
            Face f = faces.valueAt(i);
            mFaceNameMap.put(f, "put cropped bitmap here");
        }
        return faces;
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}
