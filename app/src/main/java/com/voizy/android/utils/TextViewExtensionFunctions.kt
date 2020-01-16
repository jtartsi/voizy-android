package com.voizy.android.utils

import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date

fun TextView.showTimeSecondsAndTenths(timeInMillis: Long) {
    val inMillis = (timeInMillis)
    val dateFormatter = SimpleDateFormat("s.S")
    val timeString = dateFormatter.format(Date(inMillis)).plus("s")
    text = timeString
}