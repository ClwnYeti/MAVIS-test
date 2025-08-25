package mavis.task.service.tracking

import kotlinx.coroutines.*
import mavis.task.event.Event
import mavis.task.event.HamsterEvent
import mavis.task.event.extension.handleEnter
import mavis.task.event.extension.handleExit
import mavis.task.event.extension.handleSensorFailure
import mavis.task.event.extension.handleWheelSpin
import mavis.task.state.HamsterSensorAggregate
import mavis.task.storage.Storage
import mavis.task.util.constants.ErrorMessages
import mavis.task.util.constants.LoggingMessages
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.String.format
import java.time.LocalDateTime

@Service
class HamsterEventProcessor(
    private val activityTracker: ActivityTracker,
    private val hamsterSensorAggregateStorage: Storage<HamsterSensorAggregate>
) : EventProcessor {
    private val log = LoggerFactory.getLogger(HamsterEventProcessor::class.java)

    override suspend fun startProcessing() = supervisorScope {
        activityTracker.eventsFlow.collect {
            processEvent(it)
        }
    }

    override suspend fun processEvent(event: Event) = supervisorScope {
        val currentTime = LocalDateTime.now()
        log.info(format(LoggingMessages.PROCESSING_EVENT_START, event))
        try {
            when (event) {
                is HamsterEvent.HamsterExit -> {
                    val aggregate = hamsterSensorAggregateStorage.getByIdOrThrow(event.id)
                    aggregate.handleExit(event, currentTime)
                    aggregate.events.add(HamsterSensorAggregate.HamsterEventInfo(event.id, currentTime, event))
                }

                is HamsterEvent.HamsterEnter -> {
                    val aggregate = hamsterSensorAggregateStorage.getByIdOrThrow(event.id)
                    aggregate.handleEnter(event, currentTime)
                    aggregate.events.add(HamsterSensorAggregate.HamsterEventInfo(event.id, currentTime, event))
                }

                is HamsterEvent.WheelSpin -> {
                    val aggregate = hamsterSensorAggregateStorage.getByIdOrThrow(event.id)
                    aggregate.handleWheelSpin(event, currentTime)
                    aggregate.events.add(HamsterSensorAggregate.HamsterEventInfo(event.id, currentTime, event))
                }

                is HamsterEvent.SensorFailure -> {
                    val aggregate = hamsterSensorAggregateStorage.getByIdOrThrow(event.id)
                    aggregate.handleSensorFailure(event, currentTime)
                    aggregate.events.add(HamsterSensorAggregate.HamsterEventInfo(event.id, currentTime, event))
                }
            }
            log.info(format(LoggingMessages.PROCESSING_EVENT_END, event))
        } catch (e: Exception) {
            val formattedMessage = format(ErrorMessages.ERROR_WHILE_PROCESSING, e.message)
            log.error(formattedMessage)
        } finally {
            log.info(format(LoggingMessages.PROCESSING_EVENT_END, event))
        }
    }
}
