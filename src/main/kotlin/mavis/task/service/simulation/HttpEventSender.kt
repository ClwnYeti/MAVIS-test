package mavis.task.service.simulation

import com.fasterxml.jackson.databind.ObjectMapper
import mavis.task.controller.SimulatorController
import mavis.task.event.Event
import mavis.task.event.HamsterEvent
import mavis.task.util.ServerSettings
import mavis.task.util.constants.CommonConstants
import mavis.task.util.httpHandling.HttpSendConfigurator
import mavis.task.util.constants.ErrorMessages
import mavis.task.util.exception.UnsupportedEventException
import mavis.task.util.extensions.postJsonStringWithHandlingWithoutReading
import mavis.task.util.httpHandling.RateLimiter
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.lang.String.format

@Service
class HttpEventSender(
    private val mapper: ObjectMapper,
    private val rateLimiter: RateLimiter
) : EventSender,
    HttpSendConfigurator<Mono<ResponseEntity<Void>>> {

    private val log = LoggerFactory.getLogger(HttpEventSender::class.java)


    private val client = WebClient.builder()
        .baseUrl("${ServerSettings.SERVER_HOST}:${ServerSettings.SERVER_PORT}")
        .defaultHeader(CommonConstants.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()


    override suspend fun sendEvent(event: Event): Mono<ResponseEntity<Void>> {
        return try {
            when (event) {
                is HamsterEvent.HamsterExit -> sendSerializedEvent(mapper.writeValueAsString(event as HamsterEvent.HamsterExit))
                is HamsterEvent.HamsterEnter -> sendSerializedEvent(mapper.writeValueAsString(event as HamsterEvent.HamsterEnter))
                is HamsterEvent.WheelSpin -> sendSerializedEvent(mapper.writeValueAsString(event as HamsterEvent.WheelSpin))
                is HamsterEvent.SensorFailure -> sendSerializedEvent(mapper.writeValueAsString(event as HamsterEvent.SensorFailure))
                else -> throw UnsupportedEventException(format(ErrorMessages.UNSUPPORTED_EVENT, event))
            }
        } catch(e : Exception) {
            log.error(format(ErrorMessages.ERROR_WHILE_SERIALIZATION, e.message))
            Mono.empty<ResponseEntity<Void>>()
        }
    }

    private suspend fun sendSerializedEvent(eventString: String): Mono<ResponseEntity<Void>> {
        log.info("Event was serialized to {}", eventString)
        return waitForSendingRequestWithBody(eventString)
    }

    override suspend fun sendRequestWithBody(body: String): Mono<ResponseEntity<Void>> {
        return try {
            client.postJsonStringWithHandlingWithoutReading("${SimulatorController.SIMULATOR_BASE_PATH}/${SimulatorController.SIMULATOR_EVENTS_PATH}", body) { response ->
                if (response.statusCode().is2xxSuccessful) {
                    log.info("Event was sent and handled with body: {}", body)
                    response.toBodilessEntity()
                } else {
                    throw RuntimeException("Failed to send event with status code ${response.statusCode()}")
                }
            }
        } catch (ex: Exception) {
            log.error("Error sending event: {}", ex.message, ex)
            Mono.empty<ResponseEntity<Void>>()
        }
    }

    override suspend fun checkLimits(): Boolean {
        return rateLimiter.tick()
    }

    override fun getDelayDurationMs(): Long {
        return ServerSettings.WHILE_DELAY
    }

    override fun logUnsuccessfulTickMessage(body: String) {
        log.info("Event is waiting to be sent: {}", body)
    }

    override fun logSuccessfulTickMessage(body: String) {
        log.info("Event is going to be sent: {}", body)
    }
}