package com.example.studylog.navigation.alarm.shared

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.yearMonth
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.absoluteValue

fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.year}년 ${this.month.displayText(short = short)}"
}

fun Year.displayYear(short: Boolean = false): String{
    return "${this.value}년"
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.KOREA)
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale.KOREA).let { value ->
        if (uppercase) value.uppercase(Locale.KOREA) else value
    }
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}

fun getWeekPageTitle(week: Week): String {
    val firstDate = week.days.first().date
    val lastDate = week.days.last().date
    return when {
        firstDate.yearMonth == lastDate.yearMonth -> {
            firstDate.yearMonth.displayText()
        }
        firstDate.year == lastDate.year -> {
            "${firstDate.month.displayText(short = false)} - ${lastDate.yearMonth.displayText()}"
        }
        else -> {
            "${firstDate.yearMonth.displayText()} - ${lastDate.yearMonth.displayText()}"
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun Long.toDateFormat() : String{
    val dayLong : Long = this
    val formatter = SimpleDateFormat("yyyy.MM.dd")

    return formatter.format(Date(dayLong))
}

fun Long.toTimeFormat() : String{
    val hours = this / 3600
    val min = (this % 3600) / 60
    val seconds = this % 60

    return String.format("%02d:%02d:%02d", hours, min, seconds)
}

fun LocalDateTime.toStringFormat() : String{
    return this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

fun String.toSecondFormat() : Long{
    val units = this.split(":")
    val hour = units[0].toLong()
    val min = units[1].toLong()
    val seconds = units[2].toLong()
    return hour * 3600 + min * 60 + seconds
}

fun String.toLocalDateTime() : LocalDate{
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.parse(this, dateFormatter)
}


