package com.google.android.gms.samples.vision.face.facetracker.utils

object Constants {
    object AppInfo {
        // "2019-10-28T10:41:39.8691634+08:00"
        const val SERVER_TRIMED_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val SIGN_RESULT_PAGE_TIME_FORMAT = "HH:mm:ss"
        const val SIGN_RESULT_PAGE_DATE_FORMAT = "yyyy/MM/dd E"
    }

    object Face {
        const val EYE_OPEN_VALID_PROB = 0.3
        const val FACE_VALID_CHECK_DURATION_SEC = 3L
        const val TEMP_FACE_PHOTO_NAME = "temp_face.jpg"
        const val CAMERA_SOURCE_REQUEST_FPS = 30.0f
        const val VALID_FACE_RETAIN_DURATION_MS = 200L
    }

    object Api {
        val BASE_URL = " https://facelinkapi.azurewebsites.net"

        val ERROR_TYPE_NO_FACE_DETECTED = 501
        val ERROR_TYPE_MEMEMBER_Is_EXIST = 807
    }

    enum class SignType(val type: String) {
        SIGN_IN("SignIn"), SIGN_OUT("SignOut")
    }
}