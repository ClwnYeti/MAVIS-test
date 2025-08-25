package mavis.task.state.extension

import kotlinx.coroutines.sync.withLock
import mavis.task.event.extension.handleEvents
import mavis.task.state.HamsterDailyDiff
import mavis.task.state.HamsterSensorAggregate
import mavis.task.util.extensions.nextDateStart
import java.time.LocalDate
import java.time.LocalDateTime


fun HamsterSensorAggregate.findStatisticByDate(date: LocalDate): HamsterSensorAggregate.HamsterDateStatistic? {
    return this.dateStatistics
        .firstOrNull {
            date == it.date
        }
}

fun HamsterSensorAggregate.findLastToDateTime(dateTime: LocalDateTime): HamsterSensorAggregate.HamsterDateStatistic {
    return this.dateStatistics
        .filter {
            it.date < dateTime.toLocalDate()
        }.maxByOrNull {
            it.date
        } ?: HamsterSensorAggregate.HamsterDateStatistic(
        id,
        dateTime.toLocalDate().minusDays(1),
        0,
        0,
        0,
        0,
        dateTime
    )
}

suspend fun HamsterSensorAggregate.calculateToDate(date: LocalDate): HamsterSensorAggregate.HamsterDateStatistic {
    val dateStatistic = this.findStatisticByDate(date)
    if (dateStatistic != null) {
        return dateStatistic
    }

    this.statCalculationLock.withLock {
        var lastStatistic = this.findLastToDateTime(date.atStartOfDay())

        if (date < LocalDate.now()) {
            while (lastStatistic.date < date) {
                lastStatistic = this.calculateStatisticForNextDayAfter(lastStatistic)

                this.dateStatistics.add(lastStatistic)
            }
            return lastStatistic
        } else {
            return this.calculateStatisticForNextDayAfter(lastStatistic)
        }
    }
}

suspend fun HamsterSensorAggregate.calculateStatisticForNextDayAfter(
    lastStatistic: HamsterSensorAggregate.HamsterDateStatistic
): HamsterSensorAggregate.HamsterDateStatistic {
    val nextDayStart = lastStatistic.date.nextDateStart()
    val nextDayEnd = nextDayStart.nextDateStart()
    val nextDay = nextDayStart.toLocalDate()
    val events = this.events.filter { it.dateTime >= nextDayStart && it.dateTime < nextDayEnd }
    val diff = HamsterDailyDiff(nextDay, lastStatistic)
    diff.handleEvents(events, nextDayEnd)

    return HamsterSensorAggregate.HamsterDateStatistic(
        this.id,
        nextDay,
        diff.lastStatistic.totalRunningTimeMs + diff.timeDiff,
        diff.lastStatistic.totalRounds + diff.roundsDiff,
        diff.timeDiff,
        diff.roundsDiff,
        diff.stopRunningTime
    )
}