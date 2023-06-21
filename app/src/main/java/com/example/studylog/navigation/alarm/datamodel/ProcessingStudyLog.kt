package com.example.studylog.navigation.alarm.datamodel

import android.graphics.Color
import java.time.LocalDate

data class ProcessingStudyLog(
    val subjectName: String,
    val date: LocalDate,
    val progressTime : Long,
    var color : Int = Color.BLACK,
    var percent : Float = 0.0F
)
