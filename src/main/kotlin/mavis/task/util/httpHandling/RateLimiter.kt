package mavis.task.util.httpHandling

import java.util.concurrent.TimeUnit

interface RateLimiter {
    val rate: Int
    val timeUnit: TimeUnit

    fun tick(): Boolean
}