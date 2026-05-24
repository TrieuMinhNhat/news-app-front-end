package com.example.myapplication.helper

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatter {
    fun formatVietnameseDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""

        val normalized = dateString.trim().replace(Regex("GMT([+-])(\\d{1,2})$")) { match ->
            val sign = match.groupValues[1]
            val hour = match.groupValues[2].padStart(2, '0')
            "GMT$sign$hour:00"
        }

        val patterns = listOf(
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss X",
            "EEE, dd MMM yyyy HH:mm:ss Z"
        )

        for (pattern in patterns) {
            try {
                val inputFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
                val outputFormat = SimpleDateFormat(
                    "EEEE, d 'tháng' M, yyyy",
                    Locale("vi", "VN")
                )

                val date = inputFormat.parse(normalized)
                if (date != null) {
                    return outputFormat.format(date).replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(Locale("vi", "VN")) else char.toString()
                    }
                }
            } catch (_: Exception) {
                // Try the next pattern.
            }
        }

        return normalized
    }
}
