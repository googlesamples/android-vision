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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.face.facetracker.databinding.MainBinding;
import com.google.android.gms.samples.vision.face.facetracker.ui.face.tracker.GraphicFaceTrackerFactory;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public final class FaceTrackerCameraActivity extends AppCompatActivity {

    public static final String TEMP_PHOTO_FILE_NAME = "face_track.jpg";
    public static final String EXTRA_OUTPUT = "extra_output";
    public static final String EXTRA_DEFAULT_FACING = "extra_default_facing";
    public static final String EXTRA_IS_CONTAIN_FACE = "extra_is_contain_face";
    public static final String EXTRA_IS_DRAW_FACE_TRACKING = "extra_is_draw_face_tracking";
    private static final int RC_HANDLE_GMS = 9001;
    private static final float CAMERA_SOURCE_REQUEST_FPS = 30.0f;

    private PopupMenu mSettingMenu;

    private MainBinding mBinding;
    private CameraSource mCameraSource = null;
    private Set<Face> mDetectedFaceSet = Collections.synchronizedSet(new HashSet<Face>());
    private File mOutputFile;
    private int mCurCameraFacing;
    private boolean mIsDrawFaceTracking = false;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        init();
        // Restarts the camera.
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stops the camera.
        mBinding.preview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Releases the resources associated with the camera source, the associated detector, and the rest of the processing pipeline.
        mBinding.preview.release();
        release();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_DEFAULT_FACING, mCurCameraFacing);
        outState.putString(EXTRA_OUTPUT, mOutputFile.getAbsolutePath());
        outState.putBoolean(EXTRA_IS_DRAW_FACE_TRACKING, mIsDrawFaceTracking);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Intent intent = new Intent();

        intent.putExtra(EXTRA_DEFAULT_FACING, savedInstanceState.getInt(EXTRA_DEFAULT_FACING));
        intent.putExtra(EXTRA_OUTPUT, savedInstanceState.getString(EXTRA_OUTPUT));
        intent.putExtra(EXTRA_IS_DRAW_FACE_TRACKING, savedInstanceState.getBoolean(EXTRA_IS_DRAW_FACE_TRACKING));
        setIntent(intent);
    }

    private void initView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.main);

        mBinding.ivBtnSwitch.setTag(R.drawable.ic_switch);
        initSettingsPopupMenu();
    }

    private void init() {
        Intent intent = getIntent();
        // Assign the default data
        if(intent == null) {
            intent = new Intent();
            intent.putExtra(EXTRA_OUTPUT, getExternalCacheDir() + File.separator + TEMP_PHOTO_FILE_NAME);
        }

        if(intent != null && intent.hasExtra(EXTRA_OUTPUT)) {
            String outputPath = intent.getStringExtra(EXTRA_OUTPUT);
            mOutputFile = new File(outputPath);
            mCurCameraFacing = intent.getIntExtra(EXTRA_DEFAULT_FACING,  CameraSource.CAMERA_FACING_BACK);
            mIsDrawFaceTracking = intent.getBooleanExtra(EXTRA_IS_DRAW_FACE_TRACKING, false);

            mSettingMenu.getMenu().getItem(0).setChecked(mIsDrawFaceTracking);
            createCameraSource();
        } else {
            throw new IllegalArgumentException("Miss to specify the output file uri for EXTRA_OUTPUT parameters");
        }
    }

    private void initSettingsPopupMenu() {
        mSettingMenu = new PopupMenu(this, mBinding.ivSettings);

        mSettingMenu.getMenuInflater().inflate(R.menu.menu_camera_menu, mSettingMenu.getMenu());
        mSettingMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.item_is_draw_face_tracking) {
                    mIsDrawFaceTracking = !item.isChecked();

                    item.setChecked(mIsDrawFaceTracking);
                    mBinding.preview.setIsDrawFaceTracking(mIsDrawFaceTracking);
                }
                return false;
            }
        });
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_CLASSIFICATIONS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        detector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(mBinding.faceOverlay, new GraphicFaceTrackerFactory.IFaceItamCallback() {
            @Override
            public void onNewItem(Face face) {
                mDetectedFaceSet.add(face);
            }

            @Override
            public void onUpdate(Face face) {}

            @Override
            public void onMissing(Face face) {
                mDetectedFaceSet.remove(face);
            }

            @Override
            public void onDone(Face face) {
                mDetectedFaceSet.remove(face);
            }
        })).build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Toast.makeText(this, "Face tracking is not available, plz update the GMS or restart the app", Toast.LENGTH_LONG).show();
        } else {
            mCameraSource = new CameraSource.Builder(getApplicationContext(), detector)
                    .setAutoFocusEnabled(true)
                    .setFacing(mCurCameraFacing)
                    .setRequestedFps(CAMERA_SOURCE_REQUEST_FPS)
                    .build();
        }

        mDetectedFaceSet.clear();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mBinding.preview.start(mCameraSource, mBinding.faceOverlay);
                mBinding.preview.setIsDrawFaceTracking(mIsDrawFaceTracking);
            } catch (IOException e) {
                e.printStackTrace();
                mBinding.preview.release();
            }
        }
    }

    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.iv_btn_take: {
                mBinding.preview.takePhoto(mBinding.vShutterEffect, null, new CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes) {
                        try {
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mOutputFile));

                            bos.write(bytes, 0, bytes.length);
                            bos.flush();
                            bos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(FaceTrackerCameraActivity.this, "Photo capture failed", Toast.LENGTH_LONG).show();
                        }

                        mBinding.ivBtnSwitch.setTag(R.drawable.ic_confirm);
                        mBinding.ivBtnSwitch.setImageResource(R.drawable.ic_confirm);
                        mBinding.preview.setVisibility(INVISIBLE);
                        mBinding.ivBtnTake.setVisibility(INVISIBLE);
                        mBinding.ivBtnRetry.setVisibility(VISIBLE);
                        mBinding.ivPhoto.setVisibility(VISIBLE);
                        mBinding.ivPhoto.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    }
                });
            }
            break;

            case R.id.iv_btn_switch: {
                int resId = (Integer) mBinding.ivBtnSwitch.getTag();

                if(resId == R.drawable.ic_switch) {
                    mCurCameraFacing = (mCurCameraFacing == CameraSource.CAMERA_FACING_FRONT) ? CameraSource.CAMERA_FACING_BACK : CameraSource.CAMERA_FACING_FRONT;

                    mBinding.preview.release();
                    createCameraSource();
                    startCameraSource();
                } else {
                    Intent intent = new Intent();

                    intent.putExtra(EXTRA_IS_CONTAIN_FACE, mDetectedFaceSet.size() > 0);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            break;

            case R.id.iv_btn_retry: {
                mBinding.ivBtnSwitch.setTag(R.drawable.ic_switch);
                mBinding.ivBtnSwitch.setImageResource(R.drawable.ic_switch);
                mBinding.preview.setVisibility(VISIBLE);
                mBinding.ivBtnTake.setVisibility(VISIBLE);
                mBinding.ivBtnRetry.setVisibility(INVISIBLE);
                mBinding.ivPhoto.setVisibility(INVISIBLE);
                mBinding.ivPhoto.setImageBitmap(null);

                release();
            }
            break;

            case R.id.iv_back: {
                setResult(RESULT_OK, null);
                finish();
            }
            break;

            case R.id.iv_settings: {
                mSettingMenu.show();
            }
            break;
        }
    }

    private void release() {
        mBinding.ivPhoto.setImageBitmap(null);

        BitmapDrawable drawable = (BitmapDrawable) mBinding.ivPhoto.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}
