package com.gyoheul.calm_routine.data

import java.time.format.DateTimeFormatter

object DateFormatModel {
    val notificationTimeFormat = DateTimeFormatter.ofPattern("HH:mm")!!

    val syncDataDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")!!

    val birthDateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd")!!
}