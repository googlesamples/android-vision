/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.Frame;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic implements RecognitionInterface {
    private static final String TAG = "FaceGraphic";

    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;
    
    public volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;



    private Thread mT;
    private CustomDetector mCustomDetector;
    private volatile boolean IsRecognized;
    private String mIdentity = "";

    private int FRAMES_TO_SKIP = 15;
    public int frame_cx = FRAMES_TO_SKIP; //start after n frames

    FaceGraphic(GraphicOverlay overlay, CustomDetector customDetector) {
        super(overlay);
        mCustomDetector = customDetector;
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(Color.GREEN);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(Color.MAGENTA);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();

        if (frame_cx > 0) {
            mBoxPaint.setColor(Color.MAGENTA);
            frame_cx--;
        } else {
            if (!mCustomDetector.IsBusy && !IsRecognized) { //one face at time
                int x = (int)face.getPosition().x;
                int y = (int)face.getPosition().y;
                int w = (int)face.getWidth();
                int h = (int)face.getHeight();
                mCustomDetector.setHandlerListener(this);
                mCustomDetector.startRecognition(mFaceId, x, y, w, h);
                mBoxPaint.setColor(Color.BLUE);
            }
        }
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        //Log.e(TAG, "canvas.getHeight() " + canvas.getHeight()); //1440
        //Log.e(TAG, "canvas.getWidth() " + canvas.getWidth()); //1080
        // 960 720

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        //canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        if(mIdentity != ""){
            canvas.drawText("identity: " + mIdentity, x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
        }

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        //canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        //canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

    }

    @Override
    public void onRecognized(String str) {
        frame_cx = FRAMES_TO_SKIP; //reset
        mCustomDetector.setHandlerListener(null); //unsubscribe
        mCustomDetector.resetRecognition();
        if (str == "Unknown")
        {
            Log.w(TAG, "Not Recognized");
            mBoxPaint.setColor(Color.MAGENTA);
            IsRecognized = false;
            mIdentity = str;
        }
        else{
            Log.w(TAG, "Recognized");
            mBoxPaint.setColor(Color.GREEN);
            IsRecognized = true;
            mIdentity = str;
        }
    }
}
