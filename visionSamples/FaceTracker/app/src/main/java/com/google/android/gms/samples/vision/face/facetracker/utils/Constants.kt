package com.google.android.gms.samples.vision.face.facetracker.utils

import com.google.android.gms.samples.vision.face.facetracker.BuildConfig

object Constants {
    object AppInfo {
        // "2019-10-28T10:41:39.8691634+08:00"
        const val SERVER_REPONSE_TRIMED_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val SERVER_REQUEST_DATE_FORMAT = "yyyy-MM-dd"
        const val SIGN_RESULT_PAGE_TIME_FORMAT = "HH:mm:ss"
        const val SIGN_RESULT_PAGE_DATE_FORMAT = "yyyy/MM/dd E"
        const val AVG_SIGN_STATS_X_FORMAT = "MM-dd"
        const val AVG_SIGN_STATS_Y_FORMAT = "HH:mm"
        const val SIGN_IN_CRITERIA_TIME = "09:15:00"
    }

    object Face {
        // Valid eye open probability
        const val EYE_OPEN_VALID_PROB = 0.3
        const val FACE_VALID_CHECK_THROTTLE_BUFFER_SEC = 3L
        const val CAMERA_SOURCE_REQUEST_FPS = 30.0f
        const val VALID_FACE_RETAIN_DURATION_MS = 200L
        const val TEMP_FACE_PHOTO_NAME = "temp_face.jpg"
        // Capture photo width, height in px
        const val MAX_CAPTURE_PHOTO_SIZE = 400
        // Capture photo delay
        const val CAPTURE_PHOTO_DELAY_MS = 11000L

    }

    object Api {
        val BASE_URL = BuildConfig.API_URL
        val ORIG_ID = "0B193586CD103FA7"
    }

    enum class SignType(val type: String) {
        SIGN_IN("SignIn"), SIGN_OUT("SignOut")
    }
}