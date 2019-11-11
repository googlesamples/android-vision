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
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.LargeValueFormatter
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
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.AVG_SIGN_STATS_Y_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.REPONSE_TRIMED_DATE_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.REQUEST_DATE_FORMAT
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
import kotlin.collections.LinkedHashMap

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
            val startDateStr = TimeUtils.convertDateToStr(selectedDate!!.startDate.time, REQUEST_DATE_FORMAT)
            val endDateStr = TimeUtils.convertDateToStr(selectedDate!!.endDate.time, REQUEST_DATE_FORMAT)
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

    private fun initView() {
        mBinding.sdpDatePicker.apply {
            // 日期區間選擇
            val options = SublimeOptions()
            options.setPickerToShow(DATE_PICKER)
            options.setDisplayOptions(ACTIVATE_DATE_PICKER)
            options.setCanPickDateRange(true)
            this.initializePicker(options, mDatePickerListener)
        }

        mBinding.bcSignListStats.apply {
            // 平均打卡時間, 隱藏右邊顯示左邊Y
            this.axisRight.isEnabled = false
            this.axisLeft.isEnabled = true
            // Value position
            this.setDrawValueAboveBar(true)
            // background color
            this.setBackgroundColor(Color.WHITE)
            // disable description text
            this.description.isEnabled = false
            // Pinch
            this.setPinchZoom(false)
            // Bar Shadow
            this.setDrawBarShadow(false)
            // Grid background
            this.setDrawGridBackground(false)
            // Description
            this.description.isEnabled = false
            // Extra offset around chart view
            this.extraBottomOffset = resources.getInteger(R.integer.stats_linechart_extra_bottom_offset).toFloat()

            xAxis.textColor = Color.BLACK
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
//            xAxis.textSize = resources.getDimensionPixelSize(R.dimen.stats_barchart_x_text_size).toFloat()
            xAxis.axisMinimum = 0.0f
            xAxis.axisMaximum = 8.0f
            xAxis.granularity = 1f
            xAxis.setCenterAxisLabels(true)

//            xAxis.valueFormatter = object : ValueFormatter() {
//                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
//                    // TODO:
//                    return ""
//                }
//            }

            axisLeft.textColor = ColorTemplate.getHoloBlue()
            axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            axisLeft.setDrawGridLines(true)
//            axisLeft.textSize = resources.getDimensionPixelSize(R.dimen.stats_barchart_y_text_size).toFloat()
            axisLeft.axisMinimum = 0.0f
            axisLeft.axisMaximum = 1.01f
            axisLeft.setDrawGridLines(false)
            axisLeft.valueFormatter = object :ValueFormatter() {
                override fun getFormattedValue(value: Float): String = String.format("%.0f%%", value * 100)
            }

            legend.formSize = resources.getInteger(R.integer.stats_chart_legend_form_size).toFloat()
            legend.textSize = resources.getInteger(R.integer.stats_chart_legend_text_size).toFloat()
            legend.form = Legend.LegendForm.SQUARE
        }

        mBinding.pcSignInOutListStats.apply {
            // 準點出勤分佈
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
            val endDateStr = TimeUtils.convertDateToStr(this.time, REQUEST_DATE_FORMAT)
            this.add(Calendar.DAY_OF_YEAR, -30)
            val startDateStr = TimeUtils.convertDateToStr(this.time, REQUEST_DATE_FORMAT)
            mBinding.cvDateCtrlLayout.tv_date_range.text = getString(R.string.btn_date_selection, startDateStr, endDateStr)

            getSignInOutList("", startDateStr, endDateStr)
        }
    }

    fun initSignInWeekBarChart(signInOutInfos:List<SignInOutInfo>?) {
        // BarChart
        mBinding.bcSignListStats.clear()
        if(signInOutInfos == null) {
            return
        }

        // <Week字串, Pair<List<Int>, List<Int>> (週一 ~ 週日:(準時, 遲到))
        val signInWeekStatsMap = LinkedHashMap<String, Pair<Int, Int>>()
        val weekAry = resources.getStringArray(R.array.week_array)
        weekAry.forEach {
            signInWeekStatsMap.put(it, Pair(0, 0))
        }
        signInOutInfos!!.forEach {
            // 只取簽入
            if (!it.SignType.equals(SIGN_IN.type)) {
                return@forEach
            }

            // 找時間對應的Week Day
            val curCal = Calendar.getInstance()
            curCal.time = TimeUtils.convertStrToDate(REPONSE_TRIMED_DATE_FORMAT, it.getTrimedSignDateTime())
            var weekKey = ""
            run {
                signInWeekStatsMap.keys.forEach {
                    if (it.contains(curCal.get(Calendar.DAY_OF_WEEK).toString())) {
                        weekKey = it
                        return@run
                    }
                }
            }

            // 檢查簽入時間
            val signInTimeStr = TimeUtils.convertDateToStr(curCal.time, SIGN_RESULT_PAGE_TIME_FORMAT)
            val signInStatusPair = signInWeekStatsMap.get(weekKey)
            var newNormal = signInStatusPair!!.first
            var newLate = signInStatusPair!!.second
            if (signInTimeStr.compareTo(SIGN_IN_CRITERIA_TIME) <= 0) {
                ++newNormal
            } else {
                ++newLate
            }

            signInWeekStatsMap[weekKey] = signInStatusPair?.copy(first = newNormal, second = newLate)
        }

        // 建立BarEntry List
        val normalSignInVals = ArrayList<BarEntry>()
        val lateSignInVals = ArrayList<BarEntry>()
        for ((week, signInStatsPair) in signInWeekStatsMap) {
            val total = (signInStatsPair.first + signInStatsPair.second).toFloat()
            var normalPercent = if(total == 0f) 0f else signInStatsPair.first / total
            var latePercent = if(total == 0f) 0f else signInStatsPair.second / total
            val weekXVal = week.substring(0, week.indexOf(":")).toFloat()

            normalSignInVals.add(BarEntry(weekXVal, normalPercent))
            lateSignInVals.add(BarEntry(weekXVal, latePercent))
        }

        // 建立Bar DataSet
        val normalSignInStatsSet = BarDataSet(normalSignInVals, getString(R.string.on_time_title))
        normalSignInStatsSet.setColor(Color.rgb(104, 241, 175))
        val lateSignInStatsSet = BarDataSet(lateSignInVals, getString(R.string.late_title))
        lateSignInStatsSet.setColor(Color.rgb(164, 228, 251))

        // 建立BarData
        mBinding.bcSignListStats.data = BarData(normalSignInStatsSet, lateSignInStatsSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    String.format("%.0f%%", value * 100)
            })
        }
        mBinding.bcSignListStats.data.barWidth = 0.2f
        mBinding.bcSignListStats.groupBars(0.0f, 0.08f, 0.03f)
        // Group
        mBinding.bcSignListStats.groupBars(0.0f, 0.08f, 0.03f)
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
                val date = TimeUtils.convertStrToDate(REPONSE_TRIMED_DATE_FORMAT, it.getTrimedSignDateTime())
                val signInTimeStr = TimeUtils.convertDateToStr(date, SIGN_RESULT_PAGE_TIME_FORMAT)

                if(signInTimeStr.compareTo(SIGN_IN_CRITERIA_TIME) <= 0) {
                    ++onTimeCount
                } else {
                    ++lateCount
                }
            }
        }

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(onTimeCount.toFloat(), getString(R.string.on_time_title)))
        entries.add(PieEntry(lateCount.toFloat(), getString(R.string.late_title)))

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
                initSignInWeekBarChart(signInOutInfos)
                initSignInOutPieChart(signInOutInfos)
            }, {
                it.printStackTrace()
            })

    fun onClick(v: View) {
        mBinding.sdpDatePicker.visibility = VISIBLE
    }
}