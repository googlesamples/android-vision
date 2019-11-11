package com.google.android.gms.samples.vision.face.facetracker.utils

import android.content.Context
import android.util.Base64
import android.media.ExifInterface
import android.net.Uri
import androidx.core.net.toUri
import android.graphics.*
import android.graphics.Bitmap
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import com.google.gson.Gson
import me.echodev.resizer.Resizer
import java.io.*

object Utils {

    private val sGson = Gson()

    fun <T> fromJson(jsonStr: String, type: Class<T>): T {
        return sGson.fromJson(jsonStr, type)
    }

    fun <T> toJson(obj: T, type: Class<T>): String {
        return sGson.toJson(obj, type)
    }

    fun isConnectedToNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        var isConnected = false
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetworkInfo
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }

        return isConnected
    }
}