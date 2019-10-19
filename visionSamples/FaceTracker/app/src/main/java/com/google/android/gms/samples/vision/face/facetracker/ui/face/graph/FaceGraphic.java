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
package com.google.android.gms.samples.vision.face.facetracker.ui.face.graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float FACE_LANDMARK_RADIUS = 8.0f;
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

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    public void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (mFace == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float centerX = translateX(mFace.getPosition().x + mFace.getWidth() / 2);
        float centerY = translateY(mFace.getPosition().y + mFace.getHeight() / 2);
        // Draws a bounding box around the face.
        float xOffset = scaleX(mFace.getWidth() / 2.0f);
        float yOffset = scaleY(mFace.getHeight() / 2.0f);
        float left = centerX - xOffset;
        float top = centerY - yOffset;
        float right = centerX + xOffset;
        float bottom = centerY + yOffset;

        // TODO: suit for marking static image
        //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
//        for (Landmark landmark : mFace.getLandmarks()) {
//            float cx = scaleX(landmark.getPosition().x);
//            float cy = scaleY(landmark.getPosition().y);
//            canvas.drawCircle(cx, cy, FACE_LANDMARK_RADIUS, mIdPaint);
//        }
        // Draw border outline
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
        // Draw Information
        //canvas.drawText("id: " + mFaceId, right + 50, top + 50, mIdPaint);
        canvas.drawText("left eye: " + String.format("%.2f %%", mFace.getIsLeftEyeOpenProbability() * 100), right + 50, top + 150, mIdPaint);
        canvas.drawText("right eye: " + String.format("%.2f %%", mFace.getIsRightEyeOpenProbability() * 100), right  + 50, top + 200, mIdPaint);
        canvas.drawText("happiness: " + String.format("%.2f %%", mFace.getIsSmilingProbability() * 100), right  + 50, top + 250, mIdPaint);
    }
}
