package com.google.android.gms.samples.vision.face.facetracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
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

import dlib.android.Exif;
import dlib.android.FaceRecognizer;

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
    FaceRecognizer mFaceRecognizer; //came from face tracker activity
    public Bitmap tmpBitmap;


    public Face faceToRegognize;
    public volatile boolean IsRecognitionRequested;
    public volatile boolean IsBusy;
    private Thread mT;

    private int faceid;
    private int x, y, w, h;

    CustomDetector(Detector<Face> delegate, FaceRecognizer faceRecognizer)
    {
        mDelegate = delegate;
        mFaceRecognizer = faceRecognizer; //loadNative should be already called
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

            int fHeight = frame.getMetadata().getHeight();
            int fWidth = frame.getMetadata().getWidth();
            YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21,
                    fWidth, fHeight, null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, fWidth, fHeight),
                    100, byteArrayOutputStream);
            byte[] jpegArray = byteArrayOutputStream.toByteArray();
            Bitmap tmpBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
            final Bitmap cropped;
            Matrix rot = new Matrix();
            switch (frame.getMetadata().getRotation())
            {
                case 1:
                    rot.postRotate(90);
                    cropped = Bitmap.createBitmap(tmpBitmap, y, fHeight - (x + w), h, w,
                            rot,false );
                    break;
                case 2:
                    rot.postRotate(180);
                    cropped = Bitmap.createBitmap(tmpBitmap, fWidth - (x + w),
                            fHeight - (y + h), w, h, rot, false);
                    break;
                case 3:
                    rot.postRotate(270);
                    cropped = Bitmap.createBitmap(tmpBitmap, fWidth - (y + h), x, h, w, rot, false);
                    break;
                default:
                    cropped = Bitmap.createBitmap(tmpBitmap, x, y, w, h);
                    break;
            }
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
                    String res = mFaceRecognizer.recognizeFace(cropped);
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
}
