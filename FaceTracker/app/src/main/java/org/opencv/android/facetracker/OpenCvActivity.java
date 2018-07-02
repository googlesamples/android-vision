package org.opencv.android.facetracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.samples.vision.face.facetracker.FaceTrackerActivity;
import com.google.android.gms.samples.vision.face.facetracker.R;


import tensorflow.detector.spc.CameraActivityMainSPC;

public class OpenCvActivity extends AppCompatActivity {
    private static final String TAG = "OpenCvActivity";
    private Button mBtnDetect;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        mBtnDetect = (Button) findViewById(R.id.btnDetect);

        mBtnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent myIntent = new Intent(OpenCvActivity.this, CameraActivityMainSPC.class);

                OpenCvActivity.this.startActivity(myIntent);
            }
        });
    }

}
