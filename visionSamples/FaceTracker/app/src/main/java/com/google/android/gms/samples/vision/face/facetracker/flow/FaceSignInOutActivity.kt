package com.google.android.gms.samples.vision.face.facetracker.flow

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import com.google.android.gms.samples.vision.face.facetracker.R
import com.google.android.gms.samples.vision.face.facetracker.databinding.ActivityFaceSignInOutBinding
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.SignType
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.SignType.*
import com.google.android.gms.samples.vision.face.facetracker.utils.ViewUtils
import permissions.dispatcher.*
import java.util.*


@RuntimePermissions
class FaceSignInOutActivity : AppCompatActivity() {

    private lateinit var mSettingMenu: PopupMenu
    private lateinit var mBinding: ActivityFaceSignInOutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_face_sign_in_out)
        mBinding.activity = this

        init()
        initSettingsPopupMenu()
    }

    fun init() {
        mBinding.rlIncludeTimeInfoLayout.ivSettings.visibility = VISIBLE
        mBinding.cvacClock.setCalendar(Calendar.getInstance())
            .setDiameterInDp(resources.getDimension(R.dimen.clock_face_diameter))
            .setOpacity(1.0f)
            .setShowSeconds(true)
    }

    private fun initSettingsPopupMenu() {
        mSettingMenu = PopupMenu(this, mBinding.rlIncludeTimeInfoLayout.ivSettings)

        mSettingMenu.getMenuInflater().inflate(R.menu.menu_face_sign_in_out, mSettingMenu.menu)
        mSettingMenu.setOnMenuItemClickListener {
            if (it.getItemId() == R.id.item_signing_stats) {
                FaceStatisticActivity.startActivity(this)
            }
            false
        }
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {
        Toast.makeText(this, R.string.err_camera_permission_deny, Toast.LENGTH_SHORT).show()
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    fun showRationaleForCamera(request: PermissionRequest) {
        Toast.makeText(this, R.string.err_camera_permission_deny, Toast.LENGTH_SHORT).show()
        request.proceed()
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
        val intent = Intent()
        val uri = Uri.fromParts("package", getPackageName(), null)

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.setData(uri)
        startActivity(intent)
        ViewUtils.showToast(this, getString(R.string.err_camera_permission_deny))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun startSignInOut(signType:SignType) {
        FaceScaningActivity.startActivity(this, signType.type)
    }

    fun onClick(v: View) {
        val id = v.id

        when (id) {
            R.id.btn_press_signing_in, R.id.btn_press_signing_out -> {
                val signType = if (id == R.id.btn_press_signing_in) SIGN_IN else SIGN_OUT

                startSignInOutWithPermissionCheck(signType)
            }
            R.id.iv_settings -> {
                mSettingMenu.show()
            }
        }
    }
}