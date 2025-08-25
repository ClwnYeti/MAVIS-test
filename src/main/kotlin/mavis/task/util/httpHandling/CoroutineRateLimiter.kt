package mavis.task.util.httpHandling

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import mavis.task.util.ServerSettings
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
@Scope("prototype")
class CoroutineRateLimiter(
    override val rate: Int = ServerSettings.RATE_LIMIT,
    override val timeUnit: TimeUnit = TimeUnit.SECONDS
) : RateLimiter {
    private val semaphore = Semaphore(rate)
    private val rateLimiterDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val rateLimiterScope = CoroutineScope(rateLimiterDispatcher)

    private val releaseJob = rateLimiterScope.launch {
        while (true) {
            val permitsToRelease = rate - semaphore.availablePermits
            repeat(permitsToRelease) {
                run {
                    semaphore.release()
                }
            }
            delay(timeUnit.toMillis(1))
        }
    }
    override fun tick() = semaphore.tryAcquire()
}