package mavis.task.state.extensions;

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate
import java.time.LocalDateTime
import mavis.task.state.HamsterSensorAggregate
import mavis.task.state.extension.calculateStatisticForNextDayAfter
import mavis.task.state.extension.calculateToDate
import mavis.task.state.extension.findLastToDateTime
import mavis.task.state.extension.findStatisticByDate
import org.junit.jupiter.api.Test

class HamsterStatisticTests {

    private lateinit var hamsterSensorAggregateMock: HamsterSensorAggregate

    @BeforeEach
    internal fun setUp() {
        hamsterSensorAggregateMock = mockk<HamsterSensorAggregate>()
        every { hamsterSensorAggregateMock.id } returns SENSOR_ID
    }

    companion object {
        const val SENSOR_ID = "TEST_SENSOR"
    }

    private fun createStat(
        id: String,
        date: LocalDate,
        totalRunTime: Long,
        rounds: Long
    ): HamsterSensorAggregate.HamsterDateStatistic =
        HamsterSensorAggregate.HamsterDateStatistic(
            id,
            date,
            totalRunTime,
            rounds,
            0L,
            0,
            LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, 0, 0)
        )

    @Test
    fun testFindStatisticByDateWhenDataExists() = runTest {
        val statsList = ArrayList(
            listOf(
                createStat(SENSOR_ID, LocalDate.of(2023, 7, 1), 10000, 5),
                createStat(SENSOR_ID, LocalDate.of(2023, 7, 2), 15000, 8)
            )
        )

        every { hamsterSensorAggregateMock.dateStatistics } returns statsList

        val result = hamsterSensorAggregateMock.findStatisticByDate(LocalDate.of(2023, 7, 1))
        assert(result?.totalRunningTimeMs == 10000L)
        assert(result?.totalRounds == 5L)
    }

    @Test
    fun testFindStatisticByDateWhenDataAbsent() = runTest {
        val statsList = ArrayList<HamsterSensorAggregate.HamsterDateStatistic>()
        every { hamsterSensorAggregateMock.dateStatistics } returns statsList

        val result = hamsterSensorAggregateMock.findStatisticByDate(LocalDate.of(2023, 7, 1))
        assert(result == null)
    }

    @Test
    internal fun testFindLastToDateTimeWithExistingStats() = runTest {
        val statsList = ArrayList(
            listOf(
                createStat(SENSOR_ID, LocalDate.of(2023, 7, 1), 10000, 5),
                createStat(SENSOR_ID, LocalDate.of(2023, 7, 2), 15000, 8)
            )
        )

        every { hamsterSensorAggregateMock.dateStatistics } returns statsList

        val targetDateTime = LocalDateTime.of(2023, 7, 3, 12, 0)
        val result = hamsterSensorAggregateMock.findLastToDateTime(targetDateTime)

        assert(result.totalRunningTimeMs == 15000L)
        assert(result.totalRounds == 8L)
    }

    @Test
    internal fun testFindLastToDateTimeWithEmptyStats() = runTest {
        val statsList = ArrayList<HamsterSensorAggregate.HamsterDateStatistic>()
        every { hamsterSensorAggregateMock.dateStatistics } returns statsList

        val targetDateTime = LocalDateTime.of(2023, 7, 3, 12, 0)
        val result = hamsterSensorAggregateMock.findLastToDateTime(targetDateTime)

        assert(result.totalRunningTimeMs == 0L)
        assert(result.totalRounds == 0L)
    }
}