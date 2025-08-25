package mavis.task.service.report

import mavis.task.entity.HamsterSnapshotEntity
import mavis.task.state.HamsterSensorAggregate
import java.time.LocalDate

interface DailyInfoCalculator {
    suspend fun getStatByDate(date: LocalDate): List<HamsterSensorAggregate.HamsterDateStatistic>
}