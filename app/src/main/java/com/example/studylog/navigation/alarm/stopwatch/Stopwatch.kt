package com.example.studylog.navigation.alarm.stopwatch

import java.util.*

class Stopwatch {
    var startTime = 0L
    var todayTime = 0L
    var progressTime = 0L
    var isProgress : Boolean = false
    lateinit var timerTask : Timer
    interface TickListener{
        fun onTick(nowTime : Long, todayTime : Long)
    }

    var tickListener : TickListener? = null

    fun start(){
        isProgress = true
        timerTask = kotlin.concurrent.timer(period = 1000){
            startTime += 1L
            todayTime += 1L
            progressTime += 1L
            tickListener!!.onTick(startTime, todayTime)
        }
    }

    fun pause(){
        timerTask.cancel()
        startTime = 0L
        progressTime = 0L
        todayTime = 0L
        isProgress = false
    }
}