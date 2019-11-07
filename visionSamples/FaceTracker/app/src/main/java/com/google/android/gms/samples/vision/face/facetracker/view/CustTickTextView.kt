package com.google.android.gms.samples.vision.face.facetracker.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.widget.TextView
import java.util.*
import com.google.android.gms.samples.vision.face.facetracker.R
import com.google.android.gms.samples.vision.face.facetracker.utils.TimeUtils


class CustTickTextView : TextView {

    companion object {
        val TAG = "CustTickTextView"
    }

    private lateinit var mFormat: String
    private lateinit var mTickHandler:Handler
    private val mTickRunnable = object:Runnable {
        override fun run() {
            val cal = Calendar.getInstance()

            this@CustTickTextView.post {
                this@CustTickTextView.text = TimeUtils.convertDateToStr(cal.time, mFormat)
            }
            mTickHandler.postDelayed(this, 1000)
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr)
    }

    fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        val attrs = context.obtainStyledAttributes(attrs, R.styleable.CustTickTextView, defStyleAttr, 0)
        mFormat = attrs.getString(R.styleable.CustTickTextView_output_format)
        val cal = Calendar.getInstance()
        this.text = TimeUtils.convertDateToStr(cal.time, mFormat)
        this.mTickHandler = Handler()
        // Align Time
        val curTimeStamp = cal.timeInMillis
        cal.add(Calendar.SECOND, 1)
        cal.set(Calendar.MILLISECOND, 0)
        val diffMillis = cal.timeInMillis - curTimeStamp
        mTickHandler.postDelayed(mTickRunnable, diffMillis)
    }

    fun release() {
        mTickThread.quit()
    }
}