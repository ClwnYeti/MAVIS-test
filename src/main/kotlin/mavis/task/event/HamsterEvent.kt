package mavis.task.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import mavis.task.event.HamsterEvent.*
import mavis.task.util.ServerSettings
import mavis.task.util.constants.ErrorMessages
import mavis.task.util.exception.EventLogicException
import java.lang.String.format
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = HamsterSensorInit::class, name = "HamsterSensorInit"),
    JsonSubTypes.Type(value = WheelSpin::class, name = "WheelSpin"),
    JsonSubTypes.Type(value = HamsterEnter::class, name = "HamsterEnter"),
    JsonSubTypes.Type(value = HamsterExit::class, name = "HamsterExit"),
    JsonSubTypes.Type(value = SensorFailure::class, name = "SensorFailure")
])
sealed class HamsterEvent: Event {


    data class HamsterSensorInit(@JsonProperty("id") val id:  String): HamsterEvent()
    data class WheelSpin(@JsonProperty("id") val id: String, val durationMs: Long) : HamsterEvent()
    data class HamsterEnter(@JsonProperty("id") val id: String) : HamsterEvent()
    data class HamsterExit(@JsonProperty("id") val id: String) : HamsterEvent()
    data class SensorFailure(@JsonProperty("id") val id: String, val errorCode: Int) : HamsterEvent()

    companion object {
        fun getRandomSpinningTimeLong(random: Random): Long {
            return (random.nextInt(ServerSettings.MAX_WHEEL_SPIN - ServerSettings.MIN_WHEEL_SPIN) + ServerSettings.MIN_WHEEL_SPIN).toLong()
        }
    }
}