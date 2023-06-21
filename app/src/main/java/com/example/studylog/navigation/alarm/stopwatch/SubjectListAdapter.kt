package com.example.studylog.navigation.alarm.stopwatch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studylog.R
import com.example.studylog.navigation.alarm.datamodel.StudyLogDTO

class SubjectListAdapter(val context: Context, val dataSet: List<StudyLogDTO>) :
    RecyclerView.Adapter<SubjectListAdapter.ViewHolder>() {

    interface ItemClick{
        fun onClick(studyLogDTO: StudyLogDTO)
    }
    interface ItemEdit{
        fun onClick(studyLogDTO: StudyLogDTO)
    }
    interface ItemDelete{
        fun onClick(studyLogDTO: StudyLogDTO)
    }

    var itemClick : ItemClick? = null
    var itemEdit : ItemEdit? = null
    var itemDelete : ItemDelete? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectName = view.findViewById<TextView>(R.id.subjectName)
        val editButton = view.findViewById<Button>(R.id.editButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectListAdapter.ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            itemClick?.onClick(dataSet[position])
        }

        holder.subjectName.text = dataSet[position].subjectName
        holder.editButton.setOnClickListener{
            itemEdit?.onClick(dataSet[position])
        }
        holder.deleteButton.setOnClickListener{
            itemDelete?.onClick(dataSet[position])
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

}