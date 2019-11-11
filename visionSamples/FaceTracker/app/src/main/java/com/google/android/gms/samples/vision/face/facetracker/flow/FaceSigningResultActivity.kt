package com.google.android.gms.samples.vision.face.facetracker.flow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.samples.vision.face.facetracker.R
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignInData
import com.google.android.gms.samples.vision.face.facetracker.databinding.ActivityFaceSigningResultBinding
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.REPONSE_TRIMED_DATE_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.SIGN_RESULT_PAGE_DATE_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.SIGN_RESULT_PAGE_TIME_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.TimeUtils
import java.util.*
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.SignType.*
import com.google.android.gms.samples.vision.face.facetracker.utils.Utils
import com.google.android.gms.samples.vision.face.facetracker.utils.ViewUtils

class FaceSigningResultActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityFaceSigningResultBinding
    private lateinit var mSignData: SignInData
    private var mEmployee: SignInData.Employee? = null
    private var mSignDate: Date? = null

    companion object {
        const val EXTRA_EMPLOYEE_INFO_JSON_STR = "employee_info_json_str"

        fun startActivity(ctx: Context, employeeJsonStr: String) {
            val intent = Intent(ctx, FaceSigningResultActivity::class.java)

            intent.putExtra(EXTRA_EMPLOYEE_INFO_JSON_STR, employeeJsonStr)
            ctx.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent == null || !intent.hasExtra(EXTRA_EMPLOYEE_INFO_JSON_STR)) {
            ViewUtils.showToast(this, getString(R.string.err_no_member_info))
            finish()
            return
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_face_signing_result)
        mBinding.activity = this
        init()
        initView()
    }

    private fun init() {
        mSignData = Utils.fromJson(intent.getStringExtra(EXTRA_EMPLOYEE_INFO_JSON_STR), SignInData::class.java)
        mEmployee = if (!mSignData.Employees.isEmpty()) mSignData.Employees[0] else null
        mSignDate = if (mEmployee != null) TimeUtils.convertStrToDate(REPONSE_TRIMED_DATE_FORMAT, mEmployee?.getTrimedSignDateTime() ?: "") else mSignDate
    }

    private fun initView() {
        mBinding.tvName.text = mEmployee?.EmployeeName
        mBinding.tvResuleDateInfo.text =
            TimeUtils.convertDateToStr(mSignDate!!, SIGN_RESULT_PAGE_DATE_FORMAT)
        mBinding.tvResultTimeInfo.text =
            TimeUtils.convertDateToStr(mSignDate!!, SIGN_RESULT_PAGE_TIME_FORMAT)
        if (mEmployee?.SignType.equals(SIGN_IN.type)) {
            mBinding.tvSignResult.text = getString(R.string.msg_sign_in_success)
        } else {
            mBinding.tvSignResult.text = getString(R.string.msg_sign_out_success)
        }
    }

    fun onClick(v: View) {
        finish()
    }
}