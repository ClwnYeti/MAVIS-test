package mavis.task.util.dto

import java.time.LocalDate

data class DailyReport(
    val date: LocalDate,
    val hamsterStats: List<HamsterStats>
)
