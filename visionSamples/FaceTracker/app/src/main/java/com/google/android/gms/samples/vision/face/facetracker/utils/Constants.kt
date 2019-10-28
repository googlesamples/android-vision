package com.google.android.gms.samples.vision.face.facetracker.utils

object Constants {
    object AppInfo {
        // "2019-10-28T10:41:39.8691634+08:00"
        const val SERVER_TRIMED_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val SIGN_RESULT_PAGE_DEFAULT_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss"
        const val SIGN_RESULT_PAGE_TIME_FORMAT = "HH:mm:ss"
        const val SIGN_RESULT_PAGE_DATE_FORMAT = "yyyy/MM/dd E"
    }
        const val CAMERA_SOURCE_REQUEST_FPS = 30.0f
    }
    enum class SignType(val type:String) {
        SIGN_IN("SignIn"), SIGN_OUT("SignOut")
    }
}