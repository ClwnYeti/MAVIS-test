package mavis.task.service.simulation

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import mavis.task.event.HamsterEvent
import mavis.task.state.HamsterSensorAggregate
import mavis.task.util.ServerSettings
import mavis.task.storage.Storage
import mavis.task.util.constants.CommonConstants
import org.springframework.stereotype.Service
import java.util.*

@Service
class HamsterSensorSimulator(
    private val eventSender: EventSender,
    private val hamsterSensorAggregateStorage: Storage<HamsterSensorAggregate>,
) : SensorSimulator {
    private val random = Random()

    override suspend fun startSimulation() = supervisorScope {
        while (isActive) {
            val count =
                if (ServerSettings.SENSOR_COUNT > ServerSettings.HAMSTER_COUNT) ServerSettings.SENSOR_COUNT else ServerSettings.HAMSTER_COUNT
            repeat(count) { sensorIndex ->
                launch {
                    tryToRandomizeEventForSensor(sensorIndex)
                }
            }
        }
    }

    private suspend fun tryToRandomizeEventForSensor(index: Int) = coroutineScope {
        delay(ServerSettings.WHILE_DELAY)
        val aggregate = hamsterSensorAggregateStorage.getByIndex(index) ?: return@coroutineScope
        aggregate.simulationLock.withLock {
            if (!aggregate.hasSensor || !aggregate.eventStatus.isSensorWorking) return@coroutineScope
            if (random.nextFloat() < ServerSettings.SENSOR_FAILURE_CHANCE / CommonConstants.SECONDS_IN_MINUTE) {
                eventSender.sendEvent(HamsterEvent.SensorFailure(aggregate.id, ServerSettings.SENSOR_ERROR_CODE))
                return@coroutineScope
            }

            if (aggregate.hasHamster && random.nextFloat() < ServerSettings.HAMSTER_EVENT_CHANCE / CommonConstants.SECONDS_IN_MINUTE) {
                if (!aggregate.eventStatus.isHamsterInsideWheel) {
                    eventSender.sendEvent(HamsterEvent.HamsterEnter(aggregate.id))
                } else {
                    when (random.nextInt(2)) {
                        0 -> {
                            eventSender.sendEvent(HamsterEvent.HamsterExit(aggregate.id))
                        }

                        1 -> {
                            val runningTimeMs = HamsterEvent.getRandomSpinningTimeLong(random)
                            eventSender.sendEvent(
                                HamsterEvent.WheelSpin(
                                    aggregate.id,
                                    runningTimeMs
                                )
                            )
                        }
                    }
                }
            }

            delay(ServerSettings.DELAY)
        }
    }
}