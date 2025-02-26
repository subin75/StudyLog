package com.example.studylog.navigation.alarm

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.MODE_NO_LOCALIZED_COLLATORS
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.studylog.R
import com.example.studylog.databinding.ActivityMainBinding
import com.example.studylog.databinding.FragmentAlarmBinding
import com.example.studylog.navigation.alarm.shared.ProgressDialog
import com.google.android.material.tabs.TabLayoutMediator
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AlarmFragment : Fragment() {
    private val viewModel : AlarmFragmentViewModel by activityViewModels()
    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!
    private val tabTitleArray = arrayOf(
        "스톱워치",
        "통계"
    )
    private val tabLayoutMediator : TabLayoutMediator by lazy {
        TabLayoutMediator(binding.tabLayout, binding.pager){ tab, pos ->
            tab.text = tabTitleArray[pos]
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlarmBinding.inflate(inflater)
        Log.d("AlarmFragment", "onCreateView")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressDialog = ProgressDialog(context)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        viewModel.loading.observe(viewLifecycleOwner){isProgress ->
            if(isProgress) progressDialog.show()
            else progressDialog.dismiss()
        }

        //setting tablayout
        binding.pager.adapter = ViewPagerAdapter(this@AlarmFragment)
        tabLayoutMediator.attach()

        viewModel.getNowDate()
        viewModel.getStudyLogData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Alarm", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Alarm", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.pager.removeAllViews()
        tabLayoutMediator.detach()
        Log.d("Alarm", "onDestroy")
    }
}