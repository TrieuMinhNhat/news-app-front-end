package com.example.myapplication.helper

import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

object TimeFormatter {

    fun formatRelativeTime(raw: String): String {
        return try {
            val articleTime = OffsetDateTime.parse(raw)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()

            val now = java.time.LocalDateTime.now()

            val minutes = ChronoUnit.MINUTES.between(articleTime, now)
            val hours = ChronoUnit.HOURS.between(articleTime, now)
            val days = ChronoUnit.DAYS.between(articleTime, now)

            when {
                minutes < 1 -> "Vừa xong"

                minutes < 60 -> "$minutes phút trước"

                hours < 24 -> "$hours giờ trước"

                days == 1L -> "Hôm qua"

                days < 7 -> "$days ngày trước"

                else -> {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    articleTime.format(formatter)
                }
            }

        } catch (e: Exception) {
            raw
        }
    }

    fun formatRssRelative(raw: String): String {
        return try {
            val parser = SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss Z",
                Locale.ENGLISH
            )

            val date = parser.parse(raw) ?: return raw
            val now = Date()

            val diff = now.time - date.time

            val minutes = diff / (60 * 1000)
            val hours = diff / (60 * 60 * 1000)
            val days = diff / (24 * 60 * 60 * 1000)

            when {
                minutes < 1 -> "Vừa xong"
                minutes < 60 -> "$minutes phút trước"
                hours < 24 -> "$hours giờ trước"
                days == 1L -> "Hôm qua"
                days < 7 -> "$days ngày trước"
                else -> {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formatter.format(date)
                }
            }

        } catch (e: Exception) {
            raw
        }
    }
}