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
        const val FACE_VALID_CHECK_THROTTLE_BUFFER_SEC = 3L
        const val CAMERA_SOURCE_REQUEST_FPS = 30.0f
        const val VALID_FACE_RETAIN_DURATION_MS = 200L
        const val TEMP_FACE_PHOTO_NAME = "temp_face.jpg"
        const val MAX_CAPTURE_PHOTO_SIZE = 400
    }

    object Api {
        val BASE_URL = " https://facelinkapi.azurewebsites.net"
    }

    enum class SignType(val type: String) {
        SIGN_IN("SignIn"), SIGN_OUT("SignOut")
    }
}