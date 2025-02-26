package com.example.studylog.navigation.alarm

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.studylog.navigation.alarm.statistic.ShowStatisticsFragment
import com.example.studylog.navigation.alarm.stopwatch.StopwatchFragment

private const val NUM_TABS = 2

class ViewPagerAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return StopwatchFragment()
            1 -> return ShowStatisticsFragment()
        }
        return StopwatchFragment()
    }


}