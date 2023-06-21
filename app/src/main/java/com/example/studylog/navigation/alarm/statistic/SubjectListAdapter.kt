package com.example.studylog.navigation.alarm.statistic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studylog.R
import com.example.studylog.navigation.alarm.datamodel.ProcessingStudyLog
import com.example.studylog.navigation.alarm.shared.toTimeFormat

class SubjectListAdapter(val context: Context, val dataSet: List<ProcessingStudyLog>) :
    RecyclerView.Adapter<SubjectListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorImage = view.findViewById<View>(R.id.colorImage)
        val subjectName = view.findViewById<TextView>(R.id.subjectName)
        val progressTime = view.findViewById<TextView>(R.id.progressTime)
        val percent = view.findViewById<TextView>(R.id.percent)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubjectListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subejct_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectListAdapter.ViewHolder, position: Int) {
        holder.colorImage.setBackgroundColor(dataSet[position].color)
        holder.subjectName.text = dataSet[position].subjectName
        holder.progressTime.text = dataSet[position].progressTime.toTimeFormat()
        holder.percent.text = "${dataSet[position].percent}%"
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }


}