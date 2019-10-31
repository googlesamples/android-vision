package com.google.android.gms.samples.vision.face.facetracker.flow

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appeaser.sublimepickerlibrary.SublimePicker
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions.ACTIVATE_DATE_PICKER
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions.Picker.DATE_PICKER
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker
import com.example.test.api.ApiInstMgr
import com.example.test.api.response.ApiResponse
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.gms.samples.vision.face.facetracker.R
import com.google.android.gms.samples.vision.face.facetracker.api.api.interf.IFaceLink
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignData
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignInOutInfo
import com.google.android.gms.samples.vision.face.facetracker.databinding.ActivityFaceStatisticBinding
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Api.ORIG_ID
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.AVG_SIGN_STATS_X_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.AVG_SIGN_STATS_Y_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.SERVER_REPONSE_TRIMED_DATE_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.SERVER_REQUEST_DATE_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.SIGN_IN_CRITERIA_TIME
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.SIGN_RESULT_PAGE_TIME_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.TimeUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.SignType.*
import kotlinx.android.synthetic.main.view_status_ctl_layout.view.*

// TODO: It will need to be refiend
class FaceStatisticActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityFaceStatisticBinding
    private lateinit var mApiInst: IFaceLink

    private val mDatePickerListener: SublimeListenerAdapter = object : SublimeListenerAdapter() {
        override fun onCancelled() {
            mBinding.sdpDatePicker.visibility = GONE
        }

        override fun onDateTimeRecurrenceSet(
            sublimeMaterialPicker: SublimePicker,
            selectedDate: SelectedDate?,
            hourOfDay: Int, minute: Int,
            recurrenceOption: SublimeRecurrencePicker.RecurrenceOption?,
            recurrenceRule: String?
        ) {
            val startDateStr = TimeUtils.convertDateToStr(selectedDate!!.startDate.time, SERVER_REQUEST_DATE_FORMAT)
            val endDateStr = TimeUtils.convertDateToStr(selectedDate!!.endDate.time, SERVER_REQUEST_DATE_FORMAT)
            mBinding.cvDateCtrlLayout.tv_date_range.text = getString(R.string.btn_date_selection, startDateStr, endDateStr)
            mBinding.sdpDatePicker.visibility = GONE

            getSignInOutList("", startDateStr, endDateStr)
        }
    }


    companion object {
        fun startActivity(ctx: Context) {
            val intent = Intent(ctx, FaceStatisticActivity::class.java)

            ctx.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_face_statistic)
        mBinding.activity = this

        initView()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initView() {
        mBinding.sdpDatePicker.apply {
            val options = SublimeOptions()

            options.setPickerToShow(DATE_PICKER)
            options.setDisplayOptions(ACTIVATE_DATE_PICKER)
            options.setCanPickDateRange(true)
            this.initializePicker(options, mDatePickerListener)
        }
        mBinding.lcSignListStats.apply {
            // 平均打卡時間
            this.axisRight.isEnabled = false
            this.axisLeft.isEnabled = true
            // background color
            this.setBackgroundColor(Color.WHITE)
            // disable description text
            this.description.isEnabled = false
            // enable touch gestures
            this.setTouchEnabled(true)
            // enable drag
            this.isDragEnabled = true
            // enable scale
            this.setScaleEnabled(true)
            // highlight drag
            this.isHighlightPerDragEnabled = true
            // Pinch
            this.setPinchZoom(true)
            // Grid background
            this.setDrawGridBackground(false)
            // Description
            this.description.isEnabled = false

            val xAxis = this.xAxis
            xAxis.textSize = 11f
            xAxis.textColor = Color.BLACK
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.textSize = resources.getDimensionPixelSize(R.dimen.stats_linechart_x_text_size).toFloat()
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    return TimeUtils.convertDateToStr(Date(value.toLong()), AVG_SIGN_STATS_X_FORMAT)
                }
            }

            val leftAxis = this.axisLeft
            leftAxis.textColor = ColorTemplate.getHoloBlue()
            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            leftAxis.setDrawGridLines(true)
            leftAxis.textSize = resources.getDimensionPixelSize(R.dimen.stats_linechart_y_text_size).toFloat()
            leftAxis.valueFormatter = object :ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return TimeUtils.convertDateToStr(Date(value.toLong()), AVG_SIGN_STATS_Y_FORMAT)
                }
            }

            this.legend.formSize = resources.getInteger(R.integer.stats_chart_legend_form_size).toFloat()
            this.legend.textSize = resources.getInteger(R.integer.stats_chart_legend_text_size).toFloat()
        }
        mBinding.pcSignInOutListStats.apply {
            this.setUsePercentValues(true)
            this.description.isEnabled = false
            this.isDrawHoleEnabled = false
            this.setEntryLabelTextSize(resources.getInteger(R.integer.stats_piechart_value_text_size).toFloat())
            this.setExtraOffsets(5f, 10f, 5f, 5f)
            this.dragDecelerationFrictionCoef = 0.95f
            this.setTransparentCircleColor(Color.WHITE)
            this.setTransparentCircleAlpha(110)
            this.holeRadius = 58f
            this.transparentCircleRadius = 61f
            this.setDrawCenterText(false)
            this.rotationAngle = 0f
            // enable rotation of the chart by touch
            this.isRotationEnabled = true
            this.isHighlightPerTapEnabled = true

            this.legend.formSize = resources.getInteger(R.integer.stats_chart_legend_form_size).toFloat()
            this.legend.textSize = resources.getInteger(R.integer.stats_chart_legend_text_size).toFloat()
        }
    }


    private fun init() {
        mApiInst = ApiInstMgr.getInstnace(
            this,
            Constants.Api.BASE_URL,
            IFaceLink::class.java
        )!!

        // 初始預設日期為[當日 - 30 ~ 當日)
        Calendar.getInstance().run {
            val endDateStr = TimeUtils.convertDateToStr(this.time, SERVER_REQUEST_DATE_FORMAT)
            this.add(Calendar.DAY_OF_YEAR, -30)
            val startDateStr = TimeUtils.convertDateToStr(this.time, SERVER_REQUEST_DATE_FORMAT)
            mBinding.cvDateCtrlLayout.tv_date_range.text = getString(R.string.btn_date_selection, startDateStr, endDateStr)

            getSignInOutList("", startDateStr, endDateStr)
        }
    }

    fun initSignInOutLineChart(signInOutInfos:List<SignInOutInfo>?) {
        // LineChart
        mBinding.lcSignListStats.clear()

        if(signInOutInfos == null) {
            return
        }

        // <日期字串, SignInOutInfo 列表>
        val signInDateMap = TreeMap<String, ArrayList<SignInOutInfo>>()
        val signOutDateMap = TreeMap<String, ArrayList<SignInOutInfo>>()
        signInOutInfos?.forEach {
            // 分類 SignIn/SignOut
            if(it.SignType.equals(SIGN_IN.type)) {
                if (!signInDateMap.containsKey(it.SignDate)) {
                    signInDateMap[it.SignDate] = ArrayList()
                }
                val signInList = signInDateMap[it.SignDate]

                signInList!!.add(it)
            } else {
                if (!signOutDateMap.containsKey(it.SignDate)) {
                    signOutDateMap[it.SignDate] = ArrayList()
                }
                val signOutList = signOutDateMap[it.SignDate]

                signOutList!!.add(it)
            }
        }

        val signInDataSet = LineDataSet(Collections.emptyList(), getString(R.string.btn_press_signing_in))
        val signInEntryList = ArrayList<Entry>()
        signInDataSet.setAxisDependency(YAxis.AxisDependency.LEFT)
        signInDataSet.setColor(Color.BLUE)
        signInDataSet.setCircleColor(Color.YELLOW)
        signInDataSet.setLineWidth(2f)
        signInDataSet.setCircleRadius(3f)
        signInDataSet.setFillAlpha(65)
        signInDataSet.setDrawCircleHole(false)
        signInDataSet.setHighLightColor(Color.GRAY)
        signInDateMap.forEach {
            val date = it.key
            val signInList = it.value
            var totalSignInMS = 0L

            signInList.forEach {
                totalSignInMS += TimeUtils.convertStrToDate(SERVER_REPONSE_TRIMED_DATE_FORMAT, it.getTrimedSignDateTime()).time
            }
            val avgSignInTimeStr:Long = (totalSignInMS / signInList.size)
            signInEntryList.add(Entry(TimeUtils.convertStrToDate(SERVER_REQUEST_DATE_FORMAT, date).time.toFloat(), avgSignInTimeStr.toFloat()))
        }
        signInDataSet.values = signInEntryList

        val signOutDataSet = LineDataSet(Collections.emptyList(), getString(R.string.btn_press_signing_out))
        val signOutEntryList = ArrayList<Entry>()
        signOutDataSet.setAxisDependency(YAxis.AxisDependency.LEFT)
        signOutDataSet.setColor(Color.RED)
        signOutDataSet.setCircleColor(Color.GREEN)
        signOutDataSet.setLineWidth(2f)
        signOutDataSet.setCircleRadius(3f)
        signOutDataSet.setFillAlpha(65)
        signOutDataSet.setDrawCircleHole(false)
        signOutDataSet.setHighLightColor(Color.GRAY)

        signOutDateMap.forEach {
            val date = it.key
            val signInList = it.value
            var totalSignInMS = 0L

            signInList.forEach {
                totalSignInMS += TimeUtils.convertStrToDate(SERVER_REPONSE_TRIMED_DATE_FORMAT, it.getTrimedSignDateTime()).time
            }
            val avgSignInTimeStr:Long = (totalSignInMS / signInList.size)
            signOutEntryList.add(Entry(TimeUtils.convertStrToDate(SERVER_REQUEST_DATE_FORMAT, date).time.toFloat(), avgSignInTimeStr.toFloat()))
        }
        signOutDataSet.values = signOutEntryList

        val data = LineData(signInDataSet, signOutDataSet)
        data.setValueTextColor(Color.BLACK)
        data.setValueTextSize(resources.getInteger(R.integer.stats_linechart_value_text_size).toFloat())
        data.setValueFormatter(object :ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return TimeUtils.convertDateToStr(Date(value.toLong()), AVG_SIGN_STATS_Y_FORMAT)
            }
        })
        mBinding.lcSignListStats.data = data
    }

    private fun initSignInOutPieChart(signInOutInfos:List<SignInOutInfo>?) {
        // PieChart 顯示準點出勤分佈圓餅圖
        mBinding.pcSignInOutListStats.clear()

        if(signInOutInfos == null) {
            return
        }

        var onTimeCount = 0
        var lateCount = 0
        signInOutInfos?.forEach {
            // 分類 SignIn/SignOut
            if(it.SignType.equals(SIGN_IN.type)) {
                val date = TimeUtils.convertStrToDate(SERVER_REPONSE_TRIMED_DATE_FORMAT, it.getTrimedSignDateTime())
                val signInTimeStr = TimeUtils.convertDateToStr(date, SIGN_RESULT_PAGE_TIME_FORMAT)

                if(signInTimeStr.compareTo(SIGN_IN_CRITERIA_TIME) <= 0) {
                    ++onTimeCount
                } else {
                    ++lateCount
                }
            }
        }

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(onTimeCount.toFloat(), getString(R.string.pie_on_time)))
        entries.add(PieEntry(lateCount.toFloat(), getString(R.string.pie_late)))

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.valueTextSize = resources.getInteger(R.integer.stats_piechart_value_text_size).toFloat()
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        dataSet.colors = listOf(Color.rgb(106, 167, 134), Color.rgb(140, 234, 255))

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(mBinding.pcSignInOutListStats))
        mBinding.pcSignInOutListStats.data = data
    }

    private fun getSignList(
        employeeId: String,
        signType: String,
        startDateStr: String,
        endDateStr: String
    ): Observable<ApiResponse<List<SignData>>> =
        mApiInst.getSignList(ORIG_ID, employeeId, startDateStr, endDateStr, signType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun getSignInOutList(
        employeeId: String,
        startDateStr: String,
        endDateStr: String
    ) = mApiInst.getSignInOutList(ORIG_ID, employeeId, startDateStr, endDateStr)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                var signInOutInfos:List<SignInOutInfo>? = null

                if(it.isSuccess) {
                    signInOutInfos = it.body
                }
                initSignInOutLineChart(signInOutInfos)
                initSignInOutPieChart(signInOutInfos)
            }, {
                it.printStackTrace()
            })

    fun onClick(v: View) {
        mBinding.sdpDatePicker.visibility = VISIBLE
    }
}