package com.gyoheul.calm_routine.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date

object Extension {
    fun Date.toLocalDateTime(): LocalDateTime {
        return this.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    fun Date.toUtcLocalDateTime(): LocalDateTime {
        return this.toInstant()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()
    }

    val EnumClass.NotificationType.channelId: String
        get() = when (this) {
            EnumClass.NotificationType.Routine -> "routine"
            EnumClass.NotificationType.Mood -> "mood"
            EnumClass.NotificationType.AI -> "ai"
        }

    fun LocalDateTime.getLogicalToday(resetHour: Int = 4): LocalDate {
        return if (toLocalTime() < LocalTime.of(resetHour, 0)) {
            toLocalDate().minusDays(1)
        } else {
            toLocalDate()
        }
    }
}