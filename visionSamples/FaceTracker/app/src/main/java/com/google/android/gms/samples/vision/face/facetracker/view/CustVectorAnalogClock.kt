package com.google.android.gms.samples.vision.face.facetracker.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet

class CustVectorAnalogClock: VectorAnalogClock {
    constructor(ctx: Context?):super(ctx) {
        initializeSimple()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initializeSimple()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initializeSimple()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initializeSimple()
    }
}