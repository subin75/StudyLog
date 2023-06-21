package com.example.studylog.navigation.alarm.statistic

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studylog.R
import com.example.studylog.databinding.CalendarDayBinding
import com.example.studylog.databinding.CalendarHeaderBinding
import com.example.studylog.databinding.FragmentDayStaticsBinding
import com.example.studylog.navigation.alarm.AlarmFragmentViewModel
import com.example.studylog.navigation.alarm.datamodel.ProcessingStudyLog
import com.example.studylog.navigation.alarm.shared.displayText
import com.example.studylog.navigation.alarm.shared.toTimeFormat
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.round

class DayStaticsFragment : Fragment() {
    private val TAG = DayStaticsFragment::class.java.simpleName
    private var _binding: FragmentDayStaticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlarmFragmentViewModel by activityViewModels()
    private var selectedDate: LocalDate? = null
    private var processingData: Map<LocalDate, List<ProcessingStudyLog>>? = null

    private val colorArray: IntArray by lazy {
        resources.getIntArray(R.array.rainbowColor)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDayStaticsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)

        configureBinders(daysOfWeek)

        binding.calendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendar.scrollToMonth(currentMonth)

        binding.calendar.monthScrollListener = { month ->
            binding.exFiveMonthYearText.text = month.yearMonth.displayText()

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                binding.calendar.notifyDateChanged(it)
            }
        }

        binding.exFiveNextMonthImage.setOnClickListener {
            binding.calendar.findFirstVisibleMonth()?.let {
                binding.calendar.smoothScrollToMonth(it.yearMonth.nextMonth)
            }
        }

        binding.exFivePreviousMonthImage.setOnClickListener {
            binding.calendar.findFirstVisibleMonth()?.let {
                binding.calendar.smoothScrollToMonth(it.yearMonth.previousMonth)
            }
        }

        viewModel.processData.observe(viewLifecycleOwner) {
            Log.d(TAG, "process data observe : $it")
            processingData = it
            binding.calendar.notifyCalendarChanged()
        }

        viewModel.studyLogList.observe(viewLifecycleOwner) {
            viewModel.dataProcessingForStatistic()
        }

        binding.subjectRecyclerview.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        viewModel.dataProcessingForStatistic()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        selectedDate = null
        binding.calendar.notifyCalendarChanged()
        clearSummary()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    private fun updateSummaryInfo(date: LocalDate?) {
        Log.d(TAG, "updateSummaryInfo")
        val processingDataByDate = processingData!![date]

        // setting total time text
        var totalTime = 0L
        if (processingDataByDate != null) {
            for (processingData in processingDataByDate) {
                totalTime += processingData.progressTime
            }
        }
        binding.totalTime.text = totalTime.toTimeFormat()

        // setting graph
        settingChartView(processingDataByDate, totalTime)
    }

    private fun settingChartView(processDataByDate: List<ProcessingStudyLog>?, totalTime: Long) {
        if (processDataByDate == null) {
            Log.d(TAG, "processData null")
            binding.chart.pieChartData = null
        } else {
            val dataEntries: MutableList<SliceValue> = mutableListOf()

            for ((index, processData) in processDataByDate.withIndex()) {
                val percent =
                    round((processData.progressTime.toFloat() / totalTime.toFloat()) * 100)
                dataEntries.add(
                    SliceValue(processData.progressTime.toFloat(), colorArray[index % 7])
                        .setLabel("${percent}%")
                )
                processData.color = colorArray[index % 7]
                processData.percent = percent
            }

            val pieChartData = PieChartData(dataEntries)
            pieChartData.setHasCenterCircle(true).setCenterText1("학습 통계").setCenterText1FontSize(16)

            binding.chart.pieChartData = pieChartData
        }
        settingSubject(processDataByDate)
    }

    private fun settingSubject(processDataByDate: List<ProcessingStudyLog>?) {
        val adapter = context?.let {
            SubjectListAdapter(it, processDataByDate.orEmpty().sortedByDescending {
                it.percent
            })
        }
        binding.subjectRecyclerview.adapter = adapter
    }

    private fun clearSummary() {
        binding.totalTime.text = ""
        settingChartView(null, 0)
        settingSubject(null)
    }

    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = CalendarDayBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            val binding = this@DayStaticsFragment.binding
                            binding.calendar.notifyDateChanged(day.date)
                            oldDate?.let {
                                binding.calendar.notifyDateChanged(it)
                            }
                            updateSummaryInfo(day.date)
                        }
                    }
                }
            }
        }
        binding.calendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val context = container.binding.root.context
                val textView = container.binding.exFiveDayText
                val layout = container.binding.exFiveDayLayout
                val frameLayout = container.binding.exFiveDayFrameLayout
                textView.text = data.date.dayOfMonth.toString()

                val flightTopView = container.binding.exFiveDayFlightTop
                flightTopView.background = null

                if (data.position == DayPosition.MonthDate) {
                    textView.setTextColor(Color.GRAY)
                    layout.setBackgroundResource(if (selectedDate == data.date) R.drawable.selected_bg else 0)

                    if (processingData != null) {
                        val processingDataByDate = processingData!![data.date]
                        Log.d(TAG, processingDataByDate.toString())
                        if (processingDataByDate != null) {
                            // get today totalTime
                            var sum = 0L
                            for (processingData in processingDataByDate) {
                                sum += processingData.progressTime
                            }
                            flightTopView.text = sum.toTimeFormat()
                            frameLayout.setBackgroundResource(R.color.mainColor1)
                        }else{
                            flightTopView.text = ""
                            frameLayout.setBackgroundResource(R.color.white)
                        }
                    }
                } else {
                    layout.background = null
                    flightTopView.text = ""
                    frameLayout.setBackgroundResource(R.color.white)
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalendarHeaderBinding.bind(view).legendLayout.root
        }

        val typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL)
        binding.calendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = data.yearMonth
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText(uppercase = true)
                                tv.setTextColor(Color.BLACK)
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                tv.typeface = typeFace
                            }
                    }
                }
            }
    }
}