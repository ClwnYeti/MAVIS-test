package mavis.task.event.extension

import mavis.task.event.HamsterEvent
import mavis.task.state.HamsterSensorAggregate
import mavis.task.util.ServerSettings
import mavis.task.util.constants.ErrorMessages
import mavis.task.util.exception.EventLogicException
import java.lang.String.format
import kotlin.concurrent.thread

fun HamsterSensorAggregate.checkIfSensorIsPresent() {
    if (!this.hasSensor) {
        val formattedMessage = format(ErrorMessages.NO_VALUE_FOUND, this.id)
        throw EventLogicException(formattedMessage)
    }
}

fun HamsterSensorAggregate.checkIfSensorAndHamsterArePresent() {
    if (!this.hasSensor || !this.hasHamster) {
        val formattedMessage = format(ErrorMessages.NO_VALUE_FOUND, this.id)
        throw EventLogicException(formattedMessage)
    }
}

fun HamsterSensorAggregate.checkIfSensorIsWorking() {
    if (!this.sensorInfo.isWorking) {
        val formattedMessage = format(ErrorMessages.SENSOR_IS_NOT_WORKING, this.id)
        throw EventLogicException(formattedMessage)
    }
}

fun HamsterSensorAggregate.checkIfHamsterInsideWheel() {
    if (!this.hamsterInfo.isInsideWheel) {
        val formattedMessage = format(ErrorMessages.HAMSTER_IS_NOT_INSIDE_WHEEL, this.id)
        throw EventLogicException(formattedMessage)
    }
}

fun HamsterSensorAggregate.checkIfHamsterNotInsideWheel() {
    if (this.hamsterInfo.isInsideWheel) {
        val formattedMessage = format(ErrorMessages.HAMSTER_IS_INSIDE_WHEEL, this.id)
        throw EventLogicException(formattedMessage)
    }
}

fun HamsterEvent.WheelSpin.isValid() {
    if (this.durationMs < ServerSettings.MIN_WHEEL_SPIN || this.durationMs > ServerSettings.MAX_WHEEL_SPIN) {
        val formattedMessage = format(ErrorMessages.INVALID_SPINNING_TIME, this)
        throw EventLogicException(formattedMessage)
    }
}
