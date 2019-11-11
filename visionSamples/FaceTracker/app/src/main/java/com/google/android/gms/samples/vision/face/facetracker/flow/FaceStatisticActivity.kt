package com.google.android.gms.samples.vision.face.facetracker.flow

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.gms.samples.vision.face.facetracker.R
import com.google.android.gms.samples.vision.face.facetracker.api.api.interf.IFaceLink
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignData
import com.google.android.gms.samples.vision.face.facetracker.api.api.response.model.SignInOutInfo
import com.google.android.gms.samples.vision.face.facetracker.databinding.ActivityFaceStatisticBinding
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Api.ORIG_ID
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.REPONSE_TRIMED_DATE_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.REQUEST_DATE_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.SIGN_IN_CRITERIA_TIME
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.AppInfo.SIGN_RESULT_PAGE_TIME_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Chart.BC_VALUE_FORMAT
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Chart.WEEK_STR_SPLITOR
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Chart.X_ANIMATION_DURATION
import com.google.android.gms.samples.vision.face.facetracker.utils.Constants.Chart.Y_ANIMATION_DURATION
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
            options.pickerToShow = DATE_PICKER
            options.setDisplayOptions(ACTIVATE_DATE_PICKER)
            options.setCanPickDateRange(true)
            this.initializePicker(options, mDatePickerListener)
        }

        mBinding.bcSignListStats.apply {
            // 準點出勤分佈
            mBinding.bcSignListStats.setPinchZoom(false)
            mBinding.bcSignListStats.setDrawBarShadow(false)
            mBinding.bcSignListStats.setDrawGridBackground(false)
            mBinding.bcSignListStats.description.isEnabled = false
            mBinding.bcSignListStats.extraBottomOffset = ResourcesCompat.getFloat(resources, R.dimen.stats_bc_extra_bottom_offset)
            mBinding.bcSignListStats.isAutoScaleMinMaxEnabled = true

            val xAxis = mBinding.bcSignListStats.xAxis
            xAxis.setCenterAxisLabels(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    var weekStr = ""

                    run {
                        resources.getStringArray(R.array.week_array).forEach {
                            if(it.contains(value.toInt().toString())) {
                                weekStr = it.substring(it.indexOf(WEEK_STR_SPLITOR) + 1, it.length)
                                return@run
                            }
                        }
                    }
                    return weekStr
                }
            }
            xAxis.textSize = ResourcesCompat.getFloat(resources, R.dimen.stats_bc_bar_x_text_size)
            xAxis.axisMinimum = 1.0f
            xAxis.axisMaximum = 8.0f

            val leftAxis = mBinding.bcSignListStats.axisLeft
            leftAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = String.format(BC_VALUE_FORMAT, value * 100)
            }
            leftAxis.setDrawGridLines(false)
            leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
            leftAxis.axisMaximum = 1.01f
            leftAxis.textSize = ResourcesCompat.getFloat(resources, R.dimen.stats_bc_bar_y_text_size)
            mBinding.bcSignListStats.axisRight.isEnabled = false

            val l = mBinding.bcSignListStats.legend
            l.setDrawInside(false)
            l.textSize = ResourcesCompat.getFloat(resources, R.dimen.stats_chart_legend_text_size)
            l.formSize = ResourcesCompat.getFloat(resources, R.dimen.stats_chart_legend_form_size)
        }

        mBinding.pcSignInOutListStats.apply {
            // 出勤狀況彙總
            this.setUsePercentValues(true)
            this.description.isEnabled = false
            this.isDrawHoleEnabled = false
            this.setEntryLabelTextSize(ResourcesCompat.getFloat(resources, R.dimen.stats_pc_value_text_size))
            this.setExtraOffsets(ResourcesCompat.getFloat(resources, R.dimen.stats_pc_left_extra_offset)
                , ResourcesCompat.getFloat(resources, R.dimen.stats_pc_top_extra_offset)
                , ResourcesCompat.getFloat(resources, R.dimen.stats_pc_right_extra_offset)
                , ResourcesCompat.getFloat(resources, R.dimen.stats_pc_bottom_extra_offset))
            this.dragDecelerationFrictionCoef = 0.95f
            this.setTransparentCircleColor(Color.WHITE)
            this.setTransparentCircleAlpha(110)
            this.holeRadius = ResourcesCompat.getFloat(resources, R.dimen.stats_pc_hole_radius)
            this.transparentCircleRadius = ResourcesCompat.getFloat(resources, R.dimen.stats_pc_trans_circle_radius)
            this.setDrawCenterText(false)
            this.rotationAngle = 0f
            // enable rotation of the chart by touch
            this.isRotationEnabled = true
            this.isHighlightPerTapEnabled = true

            this.legend.formSize = ResourcesCompat.getFloat(resources, R.dimen.stats_chart_legend_form_size)
            this.legend.textSize = ResourcesCompat.getFloat(resources, R.dimen.stats_chart_legend_text_size)
            this.legend.yEntrySpace = ResourcesCompat.getFloat(resources, R.dimen.stats_pc_legend_y_entry_space)
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
            val cal = Calendar.getInstance()
            cal.time = TimeUtils.convertStrToDate(REPONSE_TRIMED_DATE_FORMAT, it.getTrimedSignDateTime())
            var weekKey = ""
            run {
                signInWeekStatsMap.keys.forEach {
                    if (it.contains(cal.get(Calendar.DAY_OF_WEEK).toString())) {
                        weekKey = it
                        return@run
                    }
                }
            }

            // 檢查簽入時間
            val signInTimeStr = TimeUtils.convertDateToStr(cal.time, SIGN_RESULT_PAGE_TIME_FORMAT)
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
            val weekXVal = week.substring(0, week.indexOf(WEEK_STR_SPLITOR)).toFloat()

            normalSignInVals.add(BarEntry(weekXVal, normalPercent))
            lateSignInVals.add(BarEntry(weekXVal, latePercent))
        }

        // 建立Bar DataSet
        val normalSignInStatsSet = BarDataSet(normalSignInVals, getString(R.string.on_time_title))
        normalSignInStatsSet.valueTextSize = ResourcesCompat.getFloat(resources, R.dimen.stats_bc_value_text_size)
        normalSignInStatsSet.color = Color.rgb(106, 167, 134)
        val lateSignInStatsSet = BarDataSet(lateSignInVals, getString(R.string.late_title))
        lateSignInStatsSet.valueTextSize = ResourcesCompat.getFloat(resources, R.dimen.stats_bc_value_text_size)
        lateSignInStatsSet.color = Color.rgb(140, 234, 255)

        // 建立BarData
        mBinding.bcSignListStats.data = BarData(normalSignInStatsSet, lateSignInStatsSet).apply {
            setValueFormatter(object : ValueFormatter() {
                // 不顯示0%的直條
                override fun getFormattedValue(value: Float) = if(value <= 0.0f) "" else String.format(BC_VALUE_FORMAT, value * 100)
            })
        }
        mBinding.bcSignListStats.data.barWidth = ResourcesCompat.getFloat(resources, R.dimen.stats_bc_bar_width)
        // Group
        mBinding.bcSignListStats.groupBars(0.0f
            , ResourcesCompat.getFloat(resources, R.dimen.stats_bc_group_space)
            , ResourcesCompat.getFloat(resources, R.dimen.stats_bc_bar_space))
        mBinding.bcSignListStats.animateXY(X_ANIMATION_DURATION, Y_ANIMATION_DURATION)
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
        dataSet.valueTextSize = ResourcesCompat.getFloat(resources, R.dimen.stats_pc_value_text_size)
        dataSet.valueTextColor = Color.BLACK
        dataSet.sliceSpace = ResourcesCompat.getFloat(resources, R.dimen.stats_pc_slice_space)
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = ResourcesCompat.getFloat(resources, R.dimen.stats_pc_selection_offset)
        dataSet.colors = listOf(Color.rgb(106, 167, 134), Color.rgb(140, 234, 255))
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(mBinding.pcSignInOutListStats))
        mBinding.pcSignInOutListStats.data = data
        mBinding.pcSignInOutListStats.animateXY(X_ANIMATION_DURATION, Y_ANIMATION_DURATION)
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