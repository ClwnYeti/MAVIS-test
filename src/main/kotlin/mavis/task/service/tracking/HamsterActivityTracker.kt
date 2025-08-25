package mavis.task.service.tracking

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.withLock
import mavis.task.event.Event
import mavis.task.event.HamsterEvent
import mavis.task.event.extension.*
import mavis.task.service.report.DailyInfoCalculator
import mavis.task.state.HamsterSensorAggregate
import mavis.task.storage.Storage
import mavis.task.util.ServerSettings
import mavis.task.util.constants.ErrorMessages.Companion.ERROR_WHILE_ADDING_TO_QUEUE
import mavis.task.util.constants.LoggingMessages
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.String.format
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class HamsterActivityTracker(
    private val hamsterSensorAggregateStorage: Storage<HamsterSensorAggregate>,
    private val alertService: AlertService,
    private val dailyInfoCalculator: DailyInfoCalculator
) : ActivityTracker {
    private val log = LoggerFactory.getLogger(HamsterActivityTracker::class.java)
    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 10000)
    override val eventsFlow = _events.asSharedFlow()

    override suspend fun addEvent(event: Event) {
        try {
            when (event) {
                is HamsterEvent.HamsterEnter -> {
                    val aggregate = hamsterSensorAggregateStorage.getByIdOrThrow(event.id)

                    aggregate.eventAddingLock.withLock {
                        aggregate.checkIfSensorAndHamsterArePresent()
                        aggregate.checkIfSensorIsWorking()
                        aggregate.checkIfHamsterNotInsideWheel()

                        _events.emit(event)
                        aggregate.eventStatus.isHamsterInsideWheel = true
                    }
                }

                is HamsterEvent.HamsterExit -> {
                    val aggregate = hamsterSensorAggregateStorage.getByIdOrThrow(event.id)

                    aggregate.eventAddingLock.withLock {
                        aggregate.checkIfSensorAndHamsterArePresent()
                        aggregate.checkIfSensorIsWorking()
                        aggregate.checkIfHamsterInsideWheel()

                        _events.emit(event)
                        aggregate.eventStatus.isHamsterInsideWheel = false
                    }
                }

                is HamsterEvent.WheelSpin -> {
                    val aggregate = hamsterSensorAggregateStorage.getByIdOrThrow(event.id)


                    aggregate.eventAddingLock.withLock {
                        event.isValid()
                        aggregate.checkIfSensorAndHamsterArePresent()
                        aggregate.checkIfSensorIsWorking()
                        aggregate.checkIfHamsterInsideWheel()

                        _events.emit(event)
                    }
                }

                is HamsterEvent.SensorFailure -> {
                    val aggregate = hamsterSensorAggregateStorage.getByIdOrThrow(event.id)


                    aggregate.eventAddingLock.withLock {
                        aggregate.checkIfSensorIsPresent()
                        aggregate.checkIfSensorIsWorking()

                        _events.emit(event)
                        aggregate.eventStatus.isSensorWorking = false
                    }
                }
            }
        } catch (e: Exception) {
            val formattedMessage = format(ERROR_WHILE_ADDING_TO_QUEUE, e.message)
            log.error(formattedMessage)
        }
    }

    override suspend fun startTracking() = supervisorScope {
        while (isActive) {
            delay(60_000L)
            val currentDateTime = LocalDateTime.now()
            checkForInactiveHamsters(currentDateTime)
            checkSensorFailures(currentDateTime)
        }
    }

    override suspend fun checkDates() = supervisorScope {
        while (isActive) {
            delay(1000L)
            val currentDate = LocalDate.now()
            if (ServerSettings.LAST_SAVED_DATE.isBefore(currentDate)) {
                dailyInfoCalculator.getStatByDate(ServerSettings.LAST_SAVED_DATE)
                ServerSettings.LAST_SAVED_DATE = currentDate
            }
        }
    }


    private suspend fun checkSensorFailures(currentDateTime: LocalDateTime) {
        hamsterSensorAggregateStorage.getAll()
            .filter { it.hasSensor && currentDateTime - Duration.ofMinutes(30) > it.events.maxOf { e -> e.dateTime } }
            .forEach { state -> alertService.sendAlert(format(LoggingMessages.NO_EVENT_FROM_SENSOR, state.id)) }
    }

    private suspend fun checkForInactiveHamsters(currentDateTime: LocalDateTime) {
        hamsterSensorAggregateStorage.getAll()
            .filter { it.hasHamster && currentDateTime - Duration.ofMinutes(180) > it.hamsterInfo.stopRunningTime }
            .forEach { state -> alertService.sendAlert(format(LoggingMessages.HAMSTER_NOT_RUNNING, state.id)) }
    }
}