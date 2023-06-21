package com.example.studylog.navigation.alarm.statistic

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.studylog.R
import com.example.studylog.navigation.alarm.datamodel.MonthSummaryData
import com.example.studylog.navigation.alarm.shared.toTimeFormat

class MonthAdapter(val context: Context, val dataSet: List<MonthSummaryData>) :
    RecyclerView.Adapter<MonthAdapter.ViewHolder>() {

    interface ItemClick{
        fun onClick(position: Int, monthSummaryData: MonthSummaryData)
    }
    var itemClick : ItemClick? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val month = view.findViewById<TextView>(R.id.month)
        val time = view.findViewById<TextView>(R.id.time)
        val layout = view.findViewById<ConstraintLayout>(R.id.layout)
        val childLayout = view.findViewById<ConstraintLayout>(R.id.childLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthAdapter.ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            itemClick?.onClick(position, dataSet[position])
        }

        holder.month.text = "${dataSet[position].month}월"
        if(dataSet[position].totalTime != 0L){
            holder.time.text = dataSet[position].totalTime.toTimeFormat()
            holder.childLayout.setBackgroundResource(R.color.mainColor1)
        }

        if(dataSet[position].isClick){
            holder.layout.setBackgroundResource(R.drawable.selected_bg)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun clearSelect(){
        for(monthSummary in dataSet){
            monthSummary.isClick = false
        }
        notifyDataSetChanged()
    }
}