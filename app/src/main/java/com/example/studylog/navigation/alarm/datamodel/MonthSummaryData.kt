package com.example.studylog.navigation.alarm.datamodel

import android.graphics.Color

data class MonthSummaryData(
    val month : Int,
    var totalTime : Long = 0,
    var avgTime : Long = 0,
    var dataList: List<SubjectData>? = null,
    var isClick : Boolean = false,
){
    data class SubjectData(
        val name : String,
        val totalTimeByMonth : Long,
        var color : Int = Color.BLACK,
        var percent : Float = 0.0F
    )
}