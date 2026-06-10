package com.habitbeads.app

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun recentDays(): List<DayInfo> {
    val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dayFormat = SimpleDateFormat("EEE", Locale.US)
    val dateFormat = SimpleDateFormat("d", Locale.US)
    val todayKey = keyFormat.format(Calendar.getInstance().time)
    return (13 downTo 0).map { daysAgo ->
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
        val key = keyFormat.format(calendar.time)
        DayInfo(key, dayFormat.format(calendar.time).take(3), dateFormat.format(calendar.time), key == todayKey)
    }
}
