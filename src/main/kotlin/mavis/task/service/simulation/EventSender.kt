package mavis.task.service.simulation

import mavis.task.event.Event
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono

interface EventSender {
    suspend fun sendEvent(event: Event): Mono<ResponseEntity<Void>>
}