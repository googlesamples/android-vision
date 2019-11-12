package com.google.android.gms.samples.vision.face.facetracker

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.google.android.gms.samples.vision.face.facetracker.utils.BuglyMgr

class FaceTrackerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        BuglyMgr.buglyInit(this, BuildConfig.buglyID)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}