package com.example.studylog.navigation.alarm.statistic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.studylog.R
import com.example.studylog.databinding.FragmentShowStatisticsBinding
import com.example.studylog.navigation.alarm.AlarmFragmentViewModel
import com.google.android.material.tabs.TabLayoutMediator

class ShowStatisticsFragment : Fragment() {
    private var _binding : FragmentShowStatisticsBinding? = null
    private val binding get() = _binding!!
    private val tabTitleArray = arrayOf(
        "일",
        "월"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentShowStatisticsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewpager.adapter = FragmentAdapter(this@ShowStatisticsFragment)

        TabLayoutMediator(binding.tabLayout, binding.viewpager){ tab, pos ->
            tab.text = tabTitleArray[pos]
        }.attach()
    }
}