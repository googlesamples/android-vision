package org.opencv.android.facetracker;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.android.CameraBridgeViewBase;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.samples.vision.face.facetracker.R;
import com.google.android.gms.samples.vision.face.facetracker.FaceTrackerActivity;



public class OpenCvActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCV-Activity";
    private Mat mRgba;
    private CameraBridgeViewBase mOpenCvCameraView;
    private RelativeLayout mRelativeLayout;
    private Camera mCamera;
    HaarDetector hd = new HaarDetector();
    Button mBtnSwitch;
    int cameraId = -1;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    hd.loadNative();
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public OpenCvActivity() {
        Log.i(TAG, "Instantiated " + this.getClass());
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "called onCreate");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_open_cv);

        /* Permissions for Android 6+
        ActivityCompat.requestPermissions(OpenCvActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);
        */
        mRelativeLayout = (RelativeLayout)findViewById(R.id.OCVtopLayout);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.main_surface);

        // what are the following used for?
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        //mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


        /*
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }*/

        onListenButton();
    }


    private void onListenButton() {
        Log.d(TAG, "called onListenButton");
        mBtnSwitch = (Button) findViewById(R.id.OCVbtnSwitch);
        mBtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(OpenCvActivity.this, FaceTrackerActivity.class);
                OpenCvActivity.this.startActivity(myIntent);
            }
        });
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
    }


    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library not found");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }


    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "called onCameraFrame");

        mRgba = inputFrame.rgba();
        MatOfRect faces = new MatOfRect();

        mRgba = inputFrame.rgba();

        hd.OCvDetect(mRgba.getNativeObjAddr(), faces.getNativeObjAddr());

        return mRgba;
    }

}