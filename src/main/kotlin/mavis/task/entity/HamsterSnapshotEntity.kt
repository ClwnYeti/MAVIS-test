package mavis.task.entity

import java.time.LocalDate

data class HamsterSnapshotEntity(
    val id: Int,
    val date: LocalDate,
    val totalRunningTimeMs: Long,
    val todayRunningTimeMs: Long,
    val totalRounds: Long,
    val todayRounds: Long
)