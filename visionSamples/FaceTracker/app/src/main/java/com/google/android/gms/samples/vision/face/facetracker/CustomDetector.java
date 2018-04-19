package com.google.android.gms.samples.vision.face.facetracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;

interface RecognitionInterface
{
    void onRecognized(String obj);
}

public class CustomDetector extends Detector<Face> {
    private static final String TAG = "CustomDetector";
    private Detector<Face> mDelegate;
    public RecognitionInterface recognitionHandler;
    public Frame mFrame;
    public Bitmap tmpBitmap;


    public Face faceToRegognize;
    public volatile boolean IsRecognitionRequested;
    public volatile boolean IsBusy;
    private Thread mT;

    private int faceid;
    private int x, y, w, h;

    CustomDetector(Detector<Face> delegate)
    {
        mDelegate = delegate;
    }

    void startRecognition(int faceId, int _x, int _y, int _w, int _h) {
            faceid = faceId;
            x = _x;
            y = _y;
            w = _w;
            h = _h;
    }

    void resetRecognition()
    {
        //IsRunning = false; //call it from onRecognized
        IsBusy = false;
    }

    public void setHandlerListener(RecognitionInterface listener)
    {
        recognitionHandler = listener;
    }

    //@Override
    public SparseArray<Face> detect(Frame frame) {
        mFrame = frame;

        if (!IsBusy && y > 0 && recognitionHandler != null) {
            IsBusy = true;

            YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21,
                    frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, frame.getMetadata().getWidth(),
                    frame.getMetadata().getHeight()), 100, byteArrayOutputStream);
            byte[] jpegArray = byteArrayOutputStream.toByteArray();
            Bitmap tmpBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
            final Bitmap cropped = Bitmap.createBitmap(tmpBitmap, x, y, w, h);
//            try {
//
//                File file = new File (getExternalStorageDirectory(), "/Download/test1.bmp");
//                FileOutputStream out = new FileOutputStream(file);
//                cropped.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                out.flush();
//                out.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            mT = new Thread(new Runnable() {
                @Override
                public void run() {
                    String res = test(cropped);
                    recognitionHandler.onRecognized(res);
                }
            });
            mT.start();

        } else{
            //it is perform recognition now
            //IsRunning = false;
        }

        SparseArray<Face> faces = mDelegate.detect(frame);
        return faces;
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }


    public native String test(Bitmap bmp);
}
