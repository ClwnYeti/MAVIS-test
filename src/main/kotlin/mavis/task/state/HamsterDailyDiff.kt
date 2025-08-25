package mavis.task.state

import java.time.LocalDate
import java.time.LocalDateTime

class HamsterDailyDiff(
    val date: LocalDate,
    val lastStatistic: HamsterSensorAggregate.HamsterDateStatistic
): State {
    var roundsDiff: Long = 0
    var timeDiff: Long = 0
    var stopRunningTime = lastStatistic.lastStopRunningTime
    var lastEventTime: LocalDateTime = date.atStartOfDay()
}