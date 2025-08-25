import io.mockk.every
import io.mockk.mockkClass
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import mavis.task.event.HamsterEvent
import mavis.task.event.extension.*
import mavis.task.state.HamsterSensorAggregate
import mavis.task.util.ServerSettings
import mavis.task.util.exception.EventLogicException
import org.junit.jupiter.api.assertThrows

class HamsterCheckersTests {
    
    @Test
    fun `test checkIfSensorIsPresent throws exception when sensor is missing`() = runTest {
        val aggregate = mockkClass(HamsterSensorAggregate::class)
        
        every { aggregate.hasSensor } returns false
        every { aggregate.id } returns "sensor-id"
        
        assertThrows<EventLogicException> {
            aggregate.checkIfSensorIsPresent()
        }
    }

    @Test
    fun `test checkIfSensorAndHamsterArePresent throws exception when either one is missing`() = runTest {
        val aggregate = mockkClass(HamsterSensorAggregate::class)
        
        every { aggregate.hasSensor } returns true
        every { aggregate.hasHamster } returns false
        every { aggregate.id } returns "sensor-hamster-id"
        
        assertThrows<EventLogicException> {
            aggregate.checkIfSensorAndHamsterArePresent()
        }
    }

    @Test
    fun `test checkIfSensorIsWorking throws exception when sensor is not working`() = runTest {
        val aggregate = mockkClass(HamsterSensorAggregate::class)
        
        every { aggregate.sensorInfo.isWorking } returns false
        every { aggregate.id } returns "working-sensor-id"
        
        assertThrows<EventLogicException> {
            aggregate.checkIfSensorIsWorking()
        }
    }

    @Test
    fun `test checkIfHamsterInsideWheel throws exception when hamster is outside wheel`() = runTest {
        val aggregate = mockkClass(HamsterSensorAggregate::class)
        
        every { aggregate.hamsterInfo.isInsideWheel } returns false
        every { aggregate.id } returns "hamster-wheel-id"
        
        assertThrows<EventLogicException> {
            aggregate.checkIfHamsterInsideWheel()
        }
    }

    @Test
    fun `test checkIfHamsterNotInsideWheel throws exception when hamster is inside wheel`() = runTest {
        val aggregate = mockkClass(HamsterSensorAggregate::class)
        
        every { aggregate.hamsterInfo.isInsideWheel } returns true
        every { aggregate.id } returns "hamster-wheel-id"
        
        assertThrows<EventLogicException> {
            aggregate.checkIfHamsterNotInsideWheel()
        }
    }

    @Test
    fun `test WheelSpin event with less than min valid duration should pass with exception`() = runTest {
        val wheelSpinEvent = HamsterEvent.WheelSpin(id = "0", durationMs = (ServerSettings.MIN_WHEEL_SPIN - 1).toLong())
        
        assertThrows<EventLogicException> {
            wheelSpinEvent.isValid()
        }
    }

    @Test
    fun `test WheelSpin event with min duration should pass without exceptions`() = runTest {
        val wheelSpinEvent = HamsterEvent.WheelSpin(id = "0", durationMs = ServerSettings.MIN_WHEEL_SPIN.toLong())
        
        wheelSpinEvent.isValid()
    }

    @Test
    fun `test WheelSpin event with more than max valid duration should pass with exception`() = runTest {
        val wheelSpinEvent = HamsterEvent.WheelSpin(id = "0", durationMs = (ServerSettings.MAX_WHEEL_SPIN + 1).toLong())

        assertThrows<EventLogicException> {
            wheelSpinEvent.isValid()
        }
    }

    @Test
    fun `test WheelSpin event with max duration should pass without exceptions`() = runTest {
        val wheelSpinEvent = HamsterEvent.WheelSpin(id = "0", durationMs = ServerSettings.MAX_WHEEL_SPIN.toLong())

        wheelSpinEvent.isValid()
    }
}