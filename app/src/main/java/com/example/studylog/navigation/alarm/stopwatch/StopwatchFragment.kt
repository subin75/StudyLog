package com.example.studylog.navigation.alarm.stopwatch

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.studylog.R
import com.example.studylog.databinding.FragmentStopwatchBinding
import com.example.studylog.navigation.alarm.AlarmFragmentViewModel
import com.example.studylog.navigation.alarm.datamodel.StudyLogDTO
import com.example.studylog.navigation.alarm.shared.toDateFormat

class StopwatchFragment : Fragment() {
    private val TAG = StopwatchFragment::class.java.simpleName
    private val viewModel: AlarmFragmentViewModel by activityViewModels()
    private var _binding: FragmentStopwatchBinding? = null
    private val binding get() = _binding!!
    private lateinit var studyLogList: List<StudyLogDTO>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStopwatchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addSubButton.setOnClickListener {
            val bottomSheet = BottomSheet()
            bottomSheet.itemClick = object : BottomSheet.ItemClick {
                override fun onClick(studyLogDTO: StudyLogDTO) {
                    viewModel.selectStudyLog(studyLogDTO)
                }
            }
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        viewModel.nowDate.observe(viewLifecycleOwner) {
            binding.todayDate.text = it.toDateFormat()
        }

        viewModel.selectStudyLog.observe(viewLifecycleOwner) {
            Log.d(TAG, it.toString())
            if (it == null) {
                binding.addSubButton.text = "과목 추가"
                binding.totalTime.text = "00:00:00"
            }
            else binding.addSubButton.text = it.subjectName
            // add init code
        }

        viewModel.timerTime.observe(viewLifecycleOwner) {
            binding.nowTime.text = it
        }

        binding.startButton.setOnClickListener {
            if(viewModel.selectStudyLog.value == null) {
                Toast.makeText(context, "과목을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (viewModel.nowPlaying.value == false){
                viewModel.startTimer()
            }
            else{
                viewModel.stopTimer()
            }
        }

        viewModel.nowPlaying.observe(viewLifecycleOwner){nowPlaying->
            if(nowPlaying) binding.startButton.setImageResource(R.drawable.baseline_pause_circle_24)
            else binding.startButton.setImageResource(R.drawable.baseline_play_circle_24)
        }

        viewModel.todayTime.observe(viewLifecycleOwner){
            binding.totalTime.text = it
        }

        viewModel.studyLogList.observe(viewLifecycleOwner){
            viewModel.getTodayTime()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}