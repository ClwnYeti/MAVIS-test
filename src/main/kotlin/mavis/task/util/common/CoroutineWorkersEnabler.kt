package mavis.task.util.common

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import mavis.task.service.simulation.SensorSimulator
import mavis.task.service.tracking.ActivityTracker
import mavis.task.service.tracking.EventProcessor
import mavis.task.state.HamsterSensorAggregate
import mavis.task.storage.Storage
import mavis.task.util.ServerSettings
import mavis.task.util.constants.ErrorMessages
import mavis.task.util.dto.HamsterSettings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.coroutines.cancellation.CancellationException

@Component
open class CoroutineWorkersEnabler {
    @Autowired
    private lateinit var hamsterSensorAggregateStorage: Storage<HamsterSensorAggregate>

    @Autowired
    private lateinit var sensorSimulator: SensorSimulator

    @Autowired
    private lateinit var activityTracker: ActivityTracker

    @Autowired
    private lateinit var eventProcessor: EventProcessor

    @OptIn(ExperimentalCoroutinesApi::class)
    private val processingDispatcher = Dispatchers.IO.limitedParallelism(10000)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val simulationDispatcher = Dispatchers.IO.limitedParallelism(10000)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val trackerDispatcher = Dispatchers.IO.limitedParallelism(1)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dateChangingDispatcher = Dispatchers.IO.limitedParallelism(1)

    private val jobs: MutableList<Job> = ArrayList()

    @PostConstruct
    fun initCoroutineWorkers(
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            cancelWorkers()
            startCoroutineWorkers()
        }
    }

    private suspend fun startCoroutineWorkers() = supervisorScope {
        hamsterSensorAggregateStorage.recreate(
            HamsterSensorAggregate.generateListBySettings(
                HamsterSettings(
                    ServerSettings.HAMSTER_COUNT,
                    ServerSettings.SENSOR_COUNT
                )
            )
        )
        jobs.add(launch(CoroutineName("event-processing") + processingDispatcher) { eventProcessor.startProcessing() })
        jobs.add(launch(CoroutineName("activity-tracking") + trackerDispatcher) { activityTracker.startTracking() })
        jobs.add(launch(CoroutineName("sensor-simulation") + simulationDispatcher) { sensorSimulator.startSimulation() })
        jobs.add(launch(CoroutineName("date-checking") + dateChangingDispatcher) { activityTracker.checkDates() })
    }

    suspend fun cancelWorkers() {
        jobs.forEach { it.cancelAndJoin() }
    }
}