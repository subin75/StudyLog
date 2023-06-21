package com.example.studylog.navigation.alarm.stopwatch

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studylog.R
import com.example.studylog.databinding.BottomSheetSubjectBinding
import com.example.studylog.navigation.alarm.AlarmFragmentViewModel
import com.example.studylog.navigation.alarm.datamodel.StudyLogDTO
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_edit.*

class BottomSheet : BottomSheetDialogFragment(){
    private lateinit var binding: BottomSheetSubjectBinding
    private val viewModel: AlarmFragmentViewModel by activityViewModels()

    interface ItemClick{
        fun onClick(studyLogDTO: StudyLogDTO)
    }
    var itemClick : ItemClick? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = BottomSheetSubjectBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.addSubButton.setOnClickListener{
            openPlusDialog()
        }

        viewModel.studyLogList.observe(viewLifecycleOwner) {
            val adapter = SubjectListAdapter(requireContext(), it)
            adapter.itemClick = object : SubjectListAdapter.ItemClick{
                override fun onClick(studyLogDTO: StudyLogDTO) {
                    itemClick?.onClick(studyLogDTO)
                    this@BottomSheet.dismiss()
                }
            }
            adapter.itemEdit = object  : SubjectListAdapter.ItemEdit{
                override fun onClick(studyLogDTO: StudyLogDTO) {
                    openEditDialog(studyLogDTO)
                }
            }
            adapter.itemDelete = object : SubjectListAdapter.ItemDelete{
                override fun onClick(studyLogDTO: StudyLogDTO) {
                    viewModel.deleteSubject(studyLogDTO)
                }
            }
            binding.recyclerView.adapter = adapter
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun openEditDialog(studyLogDTO: StudyLogDTO){
        val builder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit, null)
        val editName = dialogView.findViewById<EditText>(R.id.alterName)

        builder.setView(dialogView)
            .setPositiveButton("확인"){ _, i ->
                if(editName.text.toString().isNotEmpty()) viewModel.alterSubjectName(editName.text.toString(), studyLogDTO)
            }
            .setNegativeButton("취소"){ dialogInterface, i ->

            }
            .show()
    }

    @SuppressLint("MissingInflatedId")
    private fun openPlusDialog(){
        val builder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_plus, null)
        val editName = dialogView.findViewById<EditText>(R.id.alterName)

        builder.setView(dialogView)
            .setPositiveButton("확인"){ _, i ->
                if(editName.text.toString().isNotEmpty()) viewModel.addSubject(editName.text.toString())
            }
            .setNegativeButton("취소"){ dialogInterface, i ->

            }
            .show()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupRatio(bottomSheetDialog)
        }
        return dialog
    }

    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet!!.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight()
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getBottomSheetDialogDefaultHeight(): Int {
        return getWindowHeight() * 80 / 100
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
}