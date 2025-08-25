package mavis.task.state

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mavis.task.event.HamsterEvent
import mavis.task.event.extension.*
import mavis.task.util.constants.ErrorMessages
import mavis.task.util.dto.HamsterSettings
import mavis.task.util.exception.EventLogicException
import mavis.task.util.extensions.nextDateStart
import java.lang.String.format
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class HamsterSensorAggregate(val id: String, val hasHamster: Boolean, val hasSensor: Boolean) : State {
    data class Hamster(
        val id: String,
        var isInsideWheel: Boolean,
        var stopRunningTime: LocalDateTime
    )

    data class HamsterEventSimulationStatus(
        val id: String,
        var isHamsterInsideWheel: Boolean,
        var isSensorWorking: Boolean
    )

    data class Sensor(
        val id: String,
        var isWorking: Boolean,
        var status: Int
    )

    data class HamsterDateStatistic(
        val id: String,
        val date: LocalDate,
        val totalRunningTimeMs: Long,
        val totalRounds: Long,
        val dateRunningTimeMs: Long,
        val dateRounds: Long,
        val lastStopRunningTime: LocalDateTime
    )

    data class HamsterEventInfo(
        val id: String,
        val dateTime: LocalDateTime,
        val event: HamsterEvent
    )

    val hamsterInfo: Hamster = Hamster(id, false, LocalDateTime.now())
    val sensorInfo: Sensor = Sensor(id, true, 200)
    val eventStatus: HamsterEventSimulationStatus = HamsterEventSimulationStatus(id, hamsterInfo.isInsideWheel, sensorInfo.isWorking)
    val dateStatistics: MutableList<HamsterDateStatistic> = ArrayList()
    val events: MutableList<HamsterEventInfo> = ArrayList(
        listOf(
            HamsterEventInfo(
                id,
                LocalDateTime.now(),
                HamsterEvent.HamsterSensorInit(id),
            )
        )
    )
    val simulationLock = Mutex()
    val processingLock = Mutex()
    val eventAddingLock = Mutex()
    val statCalculationLock = Mutex()

    companion object {
        fun generateListBySettings(settings: HamsterSettings): List<HamsterSensorAggregate> {
            var hamsterNeeded = settings.hamsterCount
            var sensorNeeded = settings.sensorCount
            val list: MutableList<HamsterSensorAggregate> = ArrayList()
            while (hamsterNeeded > 0 || sensorNeeded > 0) {
                val hasHamster = hamsterNeeded > 0
                val hasSensor = sensorNeeded > 0
                list.add(HamsterSensorAggregate(list.size.toString(), hasHamster, hasSensor))

                if (hasSensor) {
                    sensorNeeded--
                }
                if (hasHamster) {
                    hamsterNeeded--
                }
            }

            return list
        }
    }
}