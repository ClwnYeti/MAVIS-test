package mavis.task.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mavis.task.entity.AlertEntity
import mavis.task.entity.HamsterSnapshotEntity
import mavis.task.event.HamsterEvent
import mavis.task.repository.Repository
import mavis.task.service.tracking.ActivityTracker
import mavis.task.state.HamsterSensorAggregate
import mavis.task.storage.HamsterSensorAggregateStorage
import mavis.task.storage.Storage
import mavis.task.util.ServerSettings
import mavis.task.util.common.CoroutineWorkersEnabler
import mavis.task.util.constants.LoggingMessages
import mavis.task.util.dto.HamsterSettings
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Private

@RestController
class SimulatorController(
    private val activityTracker: ActivityTracker,
    private val coroutineWorkersEnabler: CoroutineWorkersEnabler,
    private val mapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(SimulatorController::class.java)
    private val configLock = Mutex()

    companion object {
        const val SIMULATOR_BASE_PATH = "/simulator"
        const val SIMULATOR_CONFIG_PATH = "/config"
        const val SIMULATOR_EVENTS_PATH = "/events"
    }

    @PostMapping("$SIMULATOR_BASE_PATH$SIMULATOR_CONFIG_PATH", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun updateSimulatedConfig(@RequestBody config: HamsterSettings): String {
        configLock.withLock {
            coroutineWorkersEnabler.cancelWorkers()
            log.info(LoggingMessages.CHANGING_CONFIG)
            ServerSettings.SENSOR_COUNT = config.sensorCount
            ServerSettings.HAMSTER_COUNT = config.hamsterCount
            coroutineWorkersEnabler.initCoroutineWorkers()
        }


        return "Configuration updated successfully!"
    }

    @PostMapping("$SIMULATOR_BASE_PATH$SIMULATOR_EVENTS_PATH", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun processHamsterEvent(@RequestBody eventString: String): String {

        log.info("Gotten event: {}", eventString)
        val event = mapper.readValue(eventString, HamsterEvent::class.java)
        activityTracker.addEvent(event)
        return "Event received and added to processing queue."
    }
}