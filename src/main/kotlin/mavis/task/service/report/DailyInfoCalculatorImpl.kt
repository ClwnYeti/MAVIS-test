package mavis.task.service.report

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import org.slf4j.LoggerFactory
import mavis.task.state.HamsterSensorAggregate
import mavis.task.state.extension.calculateToDate
import mavis.task.state.extension.findStatisticByDate
import mavis.task.storage.Storage
import mavis.task.util.extensions.nextDateStart
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class DailyInfoCalculatorImpl(
    private val hamsterSensorAggregateStorage: Storage<HamsterSensorAggregate>
) : DailyInfoCalculator {

    override suspend fun getStatByDate(date: LocalDate): List<HamsterSensorAggregate.HamsterDateStatistic> =
        coroutineScope {
            val currentDate = LocalDate.now()
            return@coroutineScope if (date > currentDate) {
                ArrayList()
            } else if (date == currentDate) {
                getTodayStat()
            } else {
                calculateStatTillDateTime(date.nextDateStart())
                hamsterSensorAggregateStorage.getAll().map { hsa ->
                    hsa.findStatisticByDate(date)
                        ?: HamsterSensorAggregate.HamsterDateStatistic(
                            hsa.id,
                            date,
                            0,
                            0,
                            0,
                            0,
                            date.atStartOfDay()
                        )
                }
            }
        }

    private suspend fun getTodayStat(): List<HamsterSensorAggregate.HamsterDateStatistic> {
        val currentDate = LocalDate.now()
        return hamsterSensorAggregateStorage
            .getAll()
            .asFlow()
            .filter {
                it.hasHamster
            }
            .map { hsa -> hsa.calculateToDate(currentDate) }
            .toList()
    }

    private suspend fun calculateStatTillDateTime(dateTime: LocalDateTime) {
        hamsterSensorAggregateStorage
            .getAll()
            .asFlow()
            .filter {
                it.hasHamster
            }
            .map { hsa -> hsa.calculateToDate(dateTime.toLocalDate()) }

    }
}

