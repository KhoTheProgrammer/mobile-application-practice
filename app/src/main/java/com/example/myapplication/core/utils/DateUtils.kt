package com.example.myapplication.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private const val ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
    private const val ISO_8601_FORMAT_NO_MILLIS = "yyyy-MM-dd'T'HH:mm:ss"
    private const val ISO_8601_FORMAT_TZ = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX"
    
    fun parseIso8601Date(dateString: String?): Date? {
        if (dateString.isNullOrEmpty()) return null

        val formats = listOf(
            ISO_8601_FORMAT_TZ,
            ISO_8601_FORMAT,
            ISO_8601_FORMAT_NO_MILLIS
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(dateString)
            } catch (e: Exception) {
                // Continue to next format
            }
        }
        
        // Fallback for other formats if needed or return null
        return null
    }
    
    fun formatToDisplayDate(date: Date?): String {
        if (date == null) return ""
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(date)
    }
}
