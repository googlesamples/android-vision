package com.google.android.gms.samples.vision.face.facetracker.flow

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.samples.vision.face.facetracker.R
import com.google.android.gms.samples.vision.face.facetracker.databinding.ActivityFaceSignInOutBinding
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.SignType.*
import java.util.*

class FaceSignInOutActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityFaceSignInOutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_face_sign_in_out)

        //customization
        val calendar = Calendar.getInstance()
        mBinding.cvacClock.setCalendar(calendar)
            .setDiameterInDp(resources.getDimension(R.dimen.clock_face_diameter))
            .setOpacity(1.0f)
            .setShowSeconds(true)
            .setColor(Color.BLACK)
    }

    fun onClick(v: View) {
        val id = v.id

        when (id) {
            R.id.btn_press_signing_in, R.id.btn_press_signing_out -> {
                val signType = if (id == R.id.btn_press_signing_in) SIGN_IN else SIGN_OUT

                FaceScaningActivity.startActivity(this, signType.type)
            }
            R.id.iv_settings -> {
                TODO("Not implemented")
            }
            else -> {}
        }
    }
}