package mavis.task.util.exception

import org.springframework.http.HttpStatusCode

open class HamsterException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
}