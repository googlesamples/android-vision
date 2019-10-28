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
package com.google.android.gms.samples.vision.face.facetracker.ui.face.camera;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.samples.vision.face.facetracker.R;
import com.google.android.gms.samples.vision.face.facetracker.ui.face.graph.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

public class CameraSourcePreview extends ViewGroup {

    private static final String TAG = "CameraSourcePreview";
    private static final int CAMERA_SHUTTER_EFFECT_DURATION_IN_MS = 100;

    private Context mContext;
    private SurfaceView mSurfaceView;
    private CameraSource mCameraSource;
    private GraphicOverlay mOverlay;
    private CameraSource.PictureCallback mPictureCallback;
    private CameraSource.ShutterCallback mShutterCallback;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private boolean mIsEnableShutterLight = true;


    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        mOverlay = overlay;

        if (cameraSource == null) {
            stop();
        }
        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    public void setIsDrawFaceTracking(boolean isDrawFaceTracking) {
        mOverlay.setIsDrawFaceTracking(isDrawFaceTracking);
    }

    public void setIsEnableShutterLight(boolean isEnableShutterLight) {
        mIsEnableShutterLight = isEnableShutterLight;
    }

    public void takePhoto(final View view, final CameraSource.ShutterCallback shutterCallback, final CameraSource.PictureCallback pictureCallback) {
        if (mShutterCallback == null) {
            mShutterCallback = new CameraSource.ShutterCallback() {
                @Override
                public void onShutter() {
                    if (!mIsEnableShutterLight) {
                        return;
                    }

                    Context ctx = getContext();
                    ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(view
                            , "backgroundColor"
                            , new ArgbEvaluator()
                            , ContextCompat.getColor(ctx, R.color.shutter_effect_color_start)
                            , ContextCompat.getColor(ctx, R.color.shutter_effect_color_end));
                    backgroundColorAnimator.setDuration(CAMERA_SHUTTER_EFFECT_DURATION_IN_MS);
                    backgroundColorAnimator.start();

                    if (shutterCallback != null) {
                        shutterCallback.onShutter();
                    }
                }
            };
        }

        if (mPictureCallback == null) {
            mPictureCallback = new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {
                    if (pictureCallback != null) {
                        pictureCallback.onPictureTaken(bytes);
                    }
                }
            };
        }
        mCameraSource.takePicture(mShutterCallback, mPictureCallback);
    }

    private void startIfReady() throws IOException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());

            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            mStartRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;

//        if (mCameraSource != null) {
//            Size size = mCameraSource.getPreviewSize();
//            if (size != null) {
//                width = size.getWidth();
//                height = size.getHeight();
//            }
//        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        //if (isPortraitMode()) {
        //    int tmp = width;
        //    width = height;
        //    height = tmp;
        //}

        // Computes height and width for potentially doing fit width.
        int childWidth = width;
        int childHeight = (int) (((float) width / width) * height);

        // If height is too tall using fit width, does fit height instead.
        if (childHeight > height) {
            childHeight = height;
            childWidth = (int) (((float) height / (float) height) * width);
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}
