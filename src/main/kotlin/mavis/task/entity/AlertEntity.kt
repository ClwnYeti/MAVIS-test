package mavis.task.entity

import java.time.LocalDateTime
import java.util.*

data class AlertEntity(
    val id: Int,
    val message: String,
    val dateTime: LocalDateTime
)