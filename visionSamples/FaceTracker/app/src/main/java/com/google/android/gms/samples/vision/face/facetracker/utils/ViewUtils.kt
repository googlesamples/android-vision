package com.turn2cloud.paddemo.utils

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import com.google.android.gms.samples.vision.face.facetracker.R

object ViewUtils {

    private var sProgressDialog:ProgressDialog? = null

    fun showToast(ctx: Context, msg:String) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
    }

    fun showProgressDialog(activity: Activity) {
        if (sProgressDialog == null) {
            sProgressDialog = ProgressDialog(activity)

            sProgressDialog!!.setOwnerActivity(activity)
            sProgressDialog!!.setMessage(activity.getString(R.string.msg_progress_wait))
            sProgressDialog!!.setCanceledOnTouchOutside(false)//區域外不消失
            sProgressDialog!!.setCancelable(false)//返回鍵不取消
            sProgressDialog!!.show()
        } else if (sProgressDialog != null && !sProgressDialog!!.isShowing()) {
            sProgressDialog!!.show()
        }
    }

    fun dismissProgressDialog() {
        try {
            if (sProgressDialog == null) {
                return
            }

            val ownerActivity = sProgressDialog!!.getOwnerActivity()
            if (ownerActivity == null || ownerActivity!!.isFinishing()) {
                // 若attach的activity為null或已經finish, 則跳出
                sProgressDialog!!.hide()
            } else {
                sProgressDialog!!.dismiss()
            }
        } finally {
            sProgressDialog = null
        }
    }
}