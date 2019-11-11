package com.google.android.gms.samples.vision.face.facetracker.flow

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.test.api.ApiInstMgr
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.samples.vision.face.facetracker.R
import com.google.android.gms.samples.vision.face.facetracker.api.api.interf.IFaceLink
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignInData
import com.google.android.gms.samples.vision.face.facetracker.databinding.ActivityFaceScaningBinding
import com.google.android.gms.samples.vision.face.facetracker.ui.face.tracker.GraphicFaceTrackerFactory
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Face.VALID_FACE_RETAIN_DURATION_MS
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Face.CAMERA_SOURCE_REQUEST_FPS
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Face.CAPTURE_PHOTO_DELAY_MS
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Face.EYE_OPEN_VALID_PROB
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Face.FACE_VALID_CHECK_THROTTLE_BUFFER_SEC
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Face.MAX_CAPTURE_PHOTO_SIZE
import com.google.android.gms.samples.vision.face.facetracker.utils.ImageUtils
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.turn2cloud.paddemo.utils.Utils
import com.turn2cloud.paddemo.utils.ViewUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import java.io.*
import java.util.concurrent.atomic.AtomicBoolean

class FaceScaningActivity: AppCompatActivity() {

    private val mHandler = Handler()
    private lateinit var mBinding:ActivityFaceScaningBinding
    private var mCameraSource: CameraSource? = null
    private var mMp: MediaPlayer? = null
    private lateinit var mSignType:String
    private var mIsFaceChecking:AtomicBoolean = AtomicBoolean(false)
    private val mDetectRunnable = Runnable {
        sign()
    }
    private val mCapturePhotoDelay = CAPTURE_PHOTO_DELAY_MS
    private var mStartCountTime = 0L

    companion object {
        const val EXTRA_SIGNING_TYPE = "signing_type"
        const val RC_HANDLE_GMS = 9001

        fun startActivity(ctx: Context, signingType:String) {
            val intent = Intent(ctx, FaceScaningActivity::class.java)

            intent.putExtra(EXTRA_SIGNING_TYPE, signingType)
            ctx.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_face_scaning)
        mBinding.activity = this
        init()
    }

    override fun onDestroy() {
        super.onDestroy()

        mBinding.ivCapturePhoto.drawable.run {
            val bitmapDrawable = this as BitmapDrawable?

            System.gc()
            if (bitmapDrawable?.bitmap != null && !bitmapDrawable.bitmap!!.isRecycled) {
                bitmapDrawable.bitmap.recycle()
            }
        }
        mMp?.release()
        mHandler.removeCallbacks(mDetectRunnable)
        mBinding.preview.release()
    }

    private fun init() {
        Log.d("randy", ">>> init")
        if (intent == null || !intent.hasExtra(EXTRA_SIGNING_TYPE)) {
            ViewUtils.showToast(this, getString(R.string.err_no_sign_type_info))
            finish()
            Log.d("randy", "<<< init")
            return
        }
        mSignType = intent.getStringExtra(EXTRA_SIGNING_TYPE)

        createCameraSource()
        startCameraSource()

        mBinding.ivCapturePhoto.visibility = GONE
        mBinding.ivCapturePhoto.drawable.run {
            val bitmapDrawable = this as BitmapDrawable?

            System.gc()
            if (bitmapDrawable?.bitmap != null && !bitmapDrawable.bitmap!!.isRecycled) {
                bitmapDrawable.bitmap.recycle()
            }
        }
        Log.d("randy", "<<< init")
    }

    private fun reInitOnFail() {
        Log.d("randy", ">>> reInitOnFail")
        mMp = MediaPlayer.create(this, R.raw.voice_sign_in_failed)

        mMp!!.start()
        mMp!!.setOnCompletionListener {
            init()
            mMp!!.release()
        }
        Log.d("randy", "<<< reInitOnFail")
    }

    private fun signSuccess(employeeJsonStr:String) {
        val mp = MediaPlayer.create(this, R.raw.voice_sign_in_success)

        FaceSigningResultActivity.startActivity(this@FaceScaningActivity, employeeJsonStr)
        finish()
        mp.start()
        mp.setOnCompletionListener {
            mp.release()
        }
    }

    private fun sign() {
        Log.d("randy", ">>> sign")
        mStartCountTime = System.currentTimeMillis()
        // 辨識中
        mBinding.preview.takePhoto(mBinding.root, null, { bytes ->
                val apiInst = ApiInstMgr.getInstnace(this, Constants.Api.BASE_URL, IFaceLink::class.java)!!
                var disposable:Disposable? = null
                val byteArray = ImageUtils.createScaledBmpBytes(BitmapFactory.decodeByteArray(bytes, 0, bytes.size), MAX_CAPTURE_PHOTO_SIZE, MAX_CAPTURE_PHOTO_SIZE)
                mBinding.ivCapturePhoto.visibility = VISIBLE

                mBinding.ivCapturePhoto.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                mBinding.preview.release()
                apiInst.sign(mSignType, RequestBody.create("application/octet-stream".toMediaType(), byteArray))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally {
                        mIsFaceChecking.set(false)
                        mBinding.tvDetectingProgress.text = getString(R.string.msg_detecting_progress)
                        disposable?.dispose()
                        Log.d("randy", "<<< sign")
                    }
                    .subscribe(object :Observer<SignInData> {
                        override fun onSubscribe(d: Disposable) {disposable = d}

                        override fun onNext(t: SignInData) {
                            if (!t.Employees.isEmpty()) {
                                signSuccess(Utils.toJson(t, SignInData::class.java))
                            } else {
                                // Re-initialize camera source
                                reInitOnFail()
                            }
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            reInitOnFail()
                        }

                        override fun onComplete() {}
                    })
        })
    }

    private fun checkFace(face:Face) {
        Log.d("randy", ">>> checkFace")
        if (mIsFaceChecking.get() || isFinishing || isDestroyed) {
            Log.d("randy", "<<< checkFace")
            return
        }
        mIsFaceChecking.set(true)
        // 左右眼張開小於EYE_OPEN_VALID_PROB, 視同無效資訊
        var isValid = true

        if (face.isLeftEyeOpenProbability < EYE_OPEN_VALID_PROB || face.isRightEyeOpenProbability < EYE_OPEN_VALID_PROB) {
            mBinding.tvDetectingProgress.post {
                mBinding.tvDetectingProgress.text = getString(R.string.msg_detecting_eye_not_open)
            }
            isValid = false
            mHandler.removeCallbacks(mDetectRunnable)
        }

        for (landmark in face.landmarks) {
            if (landmark.position.x < 0 || landmark.position.y < 0) {
                mBinding.tvDetectingProgress.post {
                    mBinding.tvDetectingProgress.text = getString(R.string.msg_detecting_face_out_of_range)
                }
                isValid = false
                mHandler.removeCallbacks(mDetectRunnable)
                break
            }
        }

        if (isValid && (System.currentTimeMillis() - mStartCountTime > mCapturePhotoDelay)) {
            mBinding.tvDetectingProgress.post {
                mBinding.tvDetectingProgress.text = getString(R.string.msg_face_check_progress)
            }
            mHandler.postDelayed(mDetectRunnable, VALID_FACE_RETAIN_DURATION_MS)
        } else {
            mIsFaceChecking.set(false)
        }
        Log.d("randy", "<<< checkFace, isValid = ${isValid}, System.currentTimeMillis() - mStartCountTime > mCapturePhotoDelay = ${System.currentTimeMillis() - mStartCountTime > mCapturePhotoDelay}")
    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private fun createCameraSource() {
        val detector = FaceDetector.Builder(applicationContext)
            .setTrackingEnabled(false)
            .setLandmarkType(FaceDetector.ALL_CLASSIFICATIONS)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setMode(FaceDetector.FAST_MODE)
            .build()

        detector.setProcessor(
            MultiProcessor.Builder(
                GraphicFaceTrackerFactory(
                    mBinding.faceOverlay,
                    object : GraphicFaceTrackerFactory.IFaceItamCallback {
                        override fun onNewItem(face: Face) {
                            checkFace(face)
                        }

                        override fun onUpdate(face: Face) {
                            checkFace(face)
                        }

                        override fun onMissing(face: Face) {
                            checkFace(face)
                        }

                        override fun onDone(face: Face) {
                            mBinding.tvDetectingProgress.post {
                                mBinding.tvDetectingProgress.text = getString(R.string.msg_detecting_face_out_of_range)
                            }
                        }
                    })
            ).build()
        )

        if (!detector.isOperational) {
            Toast.makeText(
                this,
                "Face tracking is not available, plz update the GMS or restart the app",
                Toast.LENGTH_LONG
            ).show()
        } else {
            mCameraSource = CameraSource.Builder(applicationContext, detector)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(CAMERA_SOURCE_REQUEST_FPS)
                .build()
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )

        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (mCameraSource != null) {
            try {
                mBinding.preview.start(mCameraSource, mBinding.faceOverlay)
                mBinding.preview.setIsDrawFaceTracking(false)
                mBinding.preview.setIsEnableShutterLight(false)
            } catch (e: IOException) {
                e.printStackTrace()
                mBinding.preview.release()
            }

        }
    }

    fun onClick(v: View) {}
}