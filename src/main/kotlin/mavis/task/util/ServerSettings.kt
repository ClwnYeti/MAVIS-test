package mavis.task.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ServerSettings {
    companion object {
        const val SERVER_HOST = "http://localhost"
        const val SERVER_PORT = 8080
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var HAMSTER_COUNT = 20000
        var SENSOR_COUNT = 20000
        const val DELAY = 1000L
        const val MIN_WHEEL_SPIN = 1000
        const val MAX_WHEEL_SPIN = 30000
        const val SENSOR_ERROR_CODE = 500
        const val SENSOR_FAILURE_CHANCE = 0.01f
        const val HAMSTER_EVENT_CHANCE = 0.1f
        const val CIRCLE_DURATION_MILLIS = 5000L
        const val RATE_LIMIT = 1000
        const val WHILE_DELAY = 10L
        var LAST_SAVED_DATE: LocalDate = LocalDate.now()
    }
}