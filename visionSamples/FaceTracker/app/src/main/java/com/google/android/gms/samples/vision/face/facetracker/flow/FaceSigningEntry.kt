package com.google.android.gms.samples.vision.face.facetracker.flow

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.samples.vision.face.facetracker.R
import com.google.android.gms.samples.vision.face.facetracker.databinding.ActivityFaceSigningEntryBinding

import java.util.*


class FaceSigningEntry: AppCompatActivity() {

    private lateinit var mBinding: ActivityFaceSigningEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_face_signing_entry)

        //customization
        val calendar = Calendar.getInstance()
        mBinding.cvacClock.setCalendar(calendar)
            .setDiameterInDp(400.0f)
            .setOpacity(1.0f)
            .setShowSeconds(true)
            .setColor(Color.BLACK)
    }
}