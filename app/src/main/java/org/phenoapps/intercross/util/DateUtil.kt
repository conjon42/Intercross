package org.phenoapps.intercross.util

import android.os.Build
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class DateUtil {
   // fun getTime(): String = LocalDateTime.now().format(
    // /       DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))

    fun getTime(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern(
                "yyyy-MM-dd_HH:mm:ss.SSS"))
    } else {
        Calendar.getInstance().time.toString()
    }
}