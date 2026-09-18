package com.gyoheul.calm_routine.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RoutineRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    var isDone: Boolean = false,
    var sortIndex: Int = 0
)