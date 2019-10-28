package com.google.android.gms.samples.vision.face.facetracker.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun convertStrToDate(srcDateFormat:String, srcDateStr:String): Date {
        val sdf = SimpleDateFormat(srcDateFormat)

        return sdf.parse(srcDateStr)
    }

    fun convertDateToStr(srcDate:Date, targetDateFormat:String): String {
        val sdf = SimpleDateFormat(targetDateFormat)

        return sdf.format(srcDate)
    }
}