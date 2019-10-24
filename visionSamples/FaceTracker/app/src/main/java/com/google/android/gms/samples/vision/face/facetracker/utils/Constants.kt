package com.google.android.gms.samples.vision.face.facetracker.utils

object Constants {
    object AppInfo {
        const val CAMERA_SOURCE_REQUEST_FPS = 30.0f
    }
    enum class SignType(val type:String) {
        SIGN_IN("SignIn"), SIGN_OUT("SignOut")
    }
}