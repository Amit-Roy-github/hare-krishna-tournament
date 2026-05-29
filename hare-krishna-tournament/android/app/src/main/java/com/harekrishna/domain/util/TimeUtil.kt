package com.harekrishna.domain.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object TimeUtil {
    // Midnight UTC of today, in epoch ms. Matches the server's day boundary
    // (BE/services/sadhanaService.js:getDayStart) so "today" agrees everywhere.
    fun todayStartUtcMs(): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE,      0)
        cal.set(Calendar.SECOND,      0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // "YYYY-MM-DD" in UTC — the day key the sync API attributes counts to.
    fun todayKeyUtc(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(todayStartUtcMs())
    }
}
