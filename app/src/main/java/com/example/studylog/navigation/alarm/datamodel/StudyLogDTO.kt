package com.example.studylog.navigation.alarm.datamodel

data class StudyLogDTO(
    val subjectName : String,
    val dataList : List<ProgressData>
){
    data class ProgressData(
        val date : String,
        val progressTime : Long
    )
}
