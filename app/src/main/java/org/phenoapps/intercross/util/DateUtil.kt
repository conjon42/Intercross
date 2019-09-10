package org.phenoapps.intercross.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class DateUtil {
   // fun getTime(): String = LocalDateTime.now().format(
    // /       DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))

    fun getTime(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern(
            "yyyy-MM-dd_HH:mm:ss.SSS"))
}