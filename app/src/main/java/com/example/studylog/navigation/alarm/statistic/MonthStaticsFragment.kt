package com.example.studylog.navigation.alarm.statistic

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studylog.R
import com.example.studylog.databinding.FragmentMonthStaticsBinding
import com.example.studylog.navigation.alarm.AlarmFragmentViewModel
import com.example.studylog.navigation.alarm.datamodel.MonthSummaryData
import com.example.studylog.navigation.alarm.datamodel.ProcessingStudyLog
import com.example.studylog.navigation.alarm.shared.displayYear
import com.example.studylog.navigation.alarm.shared.toTimeFormat
import com.kizitonwose.calendar.core.daysOfWeek
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import java.time.Month
import java.time.Year
import java.time.YearMonth
import kotlin.math.round

class MonthStaticsFragment : Fragment() {
    private val TAG = MonthStaticsFragment::class.java.simpleName
    private val viewModel: AlarmFragmentViewModel by activityViewModels()
    private var _binding: FragmentMonthStaticsBinding? = null
    private val binding get() = _binding!!
    private var currentYear = Year.now()
    private lateinit var data : Map<Int, Map<Month?, List<ProcessingStudyLog>>>
    private lateinit var dataByMonth : List<MonthSummaryData>

    private val colorArray: IntArray by lazy {
        resources.getIntArray(R.array.rainbowColor)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMonthStaticsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val gridLayoutManager = GridLayoutManager(context, 4)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.monthRecylerView.layoutManager = gridLayoutManager
        binding.exFiveMonthYearText.text = currentYear.displayYear()

        binding.exFivePreviousMonthImage.setOnClickListener {
            currentYear = currentYear.minusYears(1)
            binding.exFiveMonthYearText.text = currentYear.displayYear()
            settingMonthRecyclerview(data)
        }

        binding.exFiveNextMonthImage.setOnClickListener {
            currentYear = currentYear.plusYears(1)
            binding.exFiveMonthYearText.text = currentYear.displayYear()
            settingMonthRecyclerview(data)
        }

        viewModel.processDataMonth.observe(viewLifecycleOwner) {
            Log.d(TAG, it.toString())
            data = it
            settingMonthRecyclerview(it)
        }

        viewModel.studyLogList.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.dataProcessingForStatisticMonth()
            }
        }

        viewModel.dataProcessingForStatisticMonth()

        binding.subjectRecyclerview.layoutManager = LinearLayoutManager(context)
    }

    override fun onPause() {
        super.onPause()
        binding.selectDate.text = ""
        binding.totalTime.text = ""
        binding.avgTime.text = ""
        binding.chart.pieChartData = null
        val adapter = binding.monthRecylerView.adapter as MonthAdapter
        adapter.clearSelect()
        binding.subjectRecyclerview.adapter = context?.let { MonthAdapter(it, emptyList())}
    }

    private fun settingMonthRecyclerview(processingData : Map<Int, Map<Month?, List<ProcessingStudyLog>>>?) {
        val monthSummaryDataList = mutableListOf<MonthSummaryData>()
        val processDataByYear = processingData?.get(currentYear.value)

        for(i : Int in 1..12){
            val processDataByMonth = processDataByYear?.get(Month.of(i))
            var totalTime = 0L
            var count = 0
            val dataList = mutableListOf<MonthSummaryData.SubjectData>()

            val subjectDataGrouping = processDataByMonth?.groupBy {
                it.subjectName
            }

            subjectDataGrouping?.keys?.forEach { key ->
                val subjectListByName = subjectDataGrouping[key]
                var totalTimeBySubject = 0L
                subjectListByName?.forEach { subjectData ->
                    totalTime += subjectData.progressTime
                    totalTimeBySubject += subjectData.progressTime
                    count += 1
                }
                dataList.add(MonthSummaryData.SubjectData(key, totalTimeBySubject))
            }

            var subjectDataGroupingByDay = processDataByMonth?.groupBy {
                it.date
            }

            var subjectDataGroupingByDaySize = if(subjectDataGroupingByDay == null) 0 else subjectDataGroupingByDay.size

            val monthSummaryData = MonthSummaryData(i, totalTime, if(subjectDataGroupingByDaySize ==0) 0 else totalTime/subjectDataGroupingByDaySize, dataList)
            monthSummaryDataList.add(monthSummaryData)
        }

        dataByMonth = monthSummaryDataList

        setAdapterMonthCalendar(monthSummaryDataList)
    }

    private fun setAdapterMonthCalendar(monthSummaryDataList : List<MonthSummaryData>){
        val adapter = context?.let { MonthAdapter(it, monthSummaryDataList) }

        adapter?.itemClick =  object : MonthAdapter.ItemClick{
            override fun onClick(position: Int, selectMonthSummaryData: MonthSummaryData) {
                if(!dataByMonth[position].isClick){
                    for ((i, monthSummaryData) in dataByMonth.withIndex()){
                        if(i != position) monthSummaryData.isClick = false
                    }
                    dataByMonth[position].isClick = true
                    setAdapterMonthCalendar(monthSummaryDataList)
                }
                settingSummaryView(selectMonthSummaryData)
            }
        }
        binding.monthRecylerView.adapter = adapter
    }

    private fun settingSummaryView(monthSummaryData: MonthSummaryData){
        binding.selectDate.text = "${currentYear.value}년 ${monthSummaryData.month}월"
        binding.totalTime.text = monthSummaryData.totalTime.toTimeFormat()
        binding.avgTime.text = monthSummaryData.avgTime.toTimeFormat()
        settingChartView(monthSummaryData.dataList, monthSummaryData.totalTime)
    }

    private fun settingChartView(dataList: List<MonthSummaryData.SubjectData>?, totalTime : Long){
        if(dataList == null || dataList.isEmpty()){
            binding.chart.pieChartData = null
        }else{
            val dataEntries: MutableList<SliceValue> = mutableListOf()
            for ((index, processData) in dataList.withIndex()) {
                val percent =
                    round((processData.totalTimeByMonth.toFloat() / totalTime.toFloat()) * 100)
                dataEntries.add(
                    SliceValue(processData.totalTimeByMonth.toFloat(), colorArray[index % 7])
                        .setLabel("${percent}%")
                )
                processData.color = colorArray[index % 7]
                processData.percent = percent
            }

            val pieChartData = PieChartData(dataEntries)
            pieChartData.setHasCenterCircle(true).setCenterText1("학습 통계").setCenterText1FontSize(16)

            binding.chart.pieChartData = pieChartData
        }
        settingSubject(dataList)
    }

    private fun settingSubject(dataList: List<MonthSummaryData.SubjectData>?) {
        val adapter = context?.let {
            SubjectListMonthAdapter(it, dataList.orEmpty().sortedByDescending {
                it.percent
            })
        }
        binding.subjectRecyclerview.adapter = adapter
    }
}