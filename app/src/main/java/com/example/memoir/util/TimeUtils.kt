package com.example.memoir.util

import android.text.format.DateUtils

object TimeUtils {
    fun formatCreatedTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val duration = now - timestamp
        
        return when {
            duration < 60000 -> "just now"
            else -> DateUtils.getRelativeTimeSpanString(
                timestamp,
                now,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
        }
    }
}
