package mavis.task.util.extensions

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange
import reactor.core.publisher.Mono

suspend fun WebClient.postJsonStringWithHandlingWithoutReading(
    uri: String,
    jsonString: String,
    responseHandler: suspend (ClientResponse) -> Mono<ResponseEntity<Void>>
):  Mono<ResponseEntity<Void>> {
    return this.post()
        .uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.TEXT_PLAIN)
        .bodyValue(jsonString)
        .awaitExchange(responseHandler)
}