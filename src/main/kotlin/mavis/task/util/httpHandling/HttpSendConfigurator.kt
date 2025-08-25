package mavis.task.util.httpHandling

import kotlinx.coroutines.delay
import mavis.task.util.constants.LoggingMessages

interface HttpSendConfigurator<T> {
    suspend fun waitForSendingRequestWithBody(body: String): T {
        while (!checkLimits()) {
            logUnsuccessfulTickMessage(body)
            delay(getDelayDurationMs())
            continue
        }
        logSuccessfulTickMessage(body)
        return sendRequestWithBody(body)
    }

    suspend fun sendRequestWithBody(body: String): T
    suspend fun checkLimits(): Boolean
    fun getDelayDurationMs(): Long
    fun logUnsuccessfulTickMessage(body: String)
    fun logSuccessfulTickMessage(body: String)
}