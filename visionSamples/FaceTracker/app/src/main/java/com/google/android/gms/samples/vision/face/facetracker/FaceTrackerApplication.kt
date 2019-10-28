package com.google.android.gms.samples.vision.face.facetracker

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class FaceTrackerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}