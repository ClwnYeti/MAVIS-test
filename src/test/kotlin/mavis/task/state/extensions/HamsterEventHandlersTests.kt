package mavis.task.state.extensions

import io.mockk.*
import kotlinx.coroutines.test.runTest
import mavis.task.event.HamsterEvent
import mavis.task.event.extension.handleEvents
import mavis.task.state.HamsterDailyDiff
import mavis.task.state.HamsterSensorAggregate
import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HamsterEventHandlersTests {

    companion object {
        const val SENSOR_ID = "TEST_SENSOR"
    }

    @Test
    fun handleEventsTest() = runTest {
        val prevDate = LocalDate.now().minusDays(1)
        val dailyDiff = HamsterDailyDiff(
            LocalDate.now(),
            HamsterSensorAggregate.HamsterDateStatistic(SENSOR_ID, prevDate, 0, 0, 0, 0, prevDate.atStartOfDay())
        )
        val currentTime = LocalDateTime.now()

        val enterEvent = HamsterEvent.HamsterEnter(SENSOR_ID)
        val exitEvent = HamsterEvent.HamsterExit(SENSOR_ID)
        val spinEvent = HamsterEvent.WheelSpin(SENSOR_ID, 10000L)
        val failureEvent = HamsterEvent.SensorFailure(SENSOR_ID, 500)

        val events = listOf(
            HamsterSensorAggregate.HamsterEventInfo(SENSOR_ID, currentTime.minusHours(1), enterEvent),
            HamsterSensorAggregate.HamsterEventInfo(SENSOR_ID, currentTime.minusMinutes(30), spinEvent),
            HamsterSensorAggregate.HamsterEventInfo(SENSOR_ID, currentTime.minusSeconds(10), exitEvent),
            HamsterSensorAggregate.HamsterEventInfo(SENSOR_ID, currentTime, failureEvent)
        )

        dailyDiff.handleEvents(events, currentTime)

        assert(dailyDiff.timeDiff > 0)
        assert(dailyDiff.roundsDiff > 0)
    }
}