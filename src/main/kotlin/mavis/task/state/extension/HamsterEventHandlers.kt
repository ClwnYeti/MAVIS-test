package mavis.task.event.extension

import kotlinx.coroutines.sync.withLock
import mavis.task.event.HamsterEvent
import mavis.task.state.HamsterDailyDiff
import mavis.task.state.HamsterSensorAggregate
import mavis.task.util.ServerSettings
import java.time.Duration
import java.time.LocalDateTime

suspend fun HamsterSensorAggregate.handleExit(event: HamsterEvent.HamsterExit, dateTime: LocalDateTime) {
    this.processingLock.withLock {
        this.checkIfSensorAndHamsterArePresent()
        this.checkIfSensorIsWorking()
        this.checkIfHamsterInsideWheel()

        this.hamsterInfo.isInsideWheel = false
        this.hamsterInfo.stopRunningTime = dateTime
        this.events.add(HamsterSensorAggregate.HamsterEventInfo(this.id, dateTime, event))
    }
}

suspend fun HamsterSensorAggregate.handleEnter(event: HamsterEvent.HamsterEnter, dateTime: LocalDateTime) {
    this.processingLock.withLock {
        this.checkIfSensorAndHamsterArePresent()
        this.checkIfSensorIsWorking()
        this.checkIfHamsterNotInsideWheel()

        this.hamsterInfo.isInsideWheel = true
        this.events.add(HamsterSensorAggregate.HamsterEventInfo(this.id, dateTime, event))
    }
}

suspend fun HamsterSensorAggregate.handleWheelSpin(event: HamsterEvent.WheelSpin, dateTime: LocalDateTime) {
    this.processingLock.withLock {
        this.checkIfSensorAndHamsterArePresent()
        this.checkIfSensorIsWorking()
        this.checkIfHamsterInsideWheel()

        val expectedStopTime = dateTime + Duration.ofMillis(event.durationMs)
        this.hamsterInfo.stopRunningTime = expectedStopTime
        this.events.add(HamsterSensorAggregate.HamsterEventInfo(this.id, dateTime, event))
    }
}

suspend fun HamsterSensorAggregate.handleSensorFailure(event: HamsterEvent.SensorFailure, dateTime: LocalDateTime) {
    this.processingLock.withLock {
        this.checkIfSensorIsPresent()
        this.checkIfSensorIsWorking()

        this.sensorInfo.isWorking = false
        this.sensorInfo.status = event.errorCode
        this.events.add(HamsterSensorAggregate.HamsterEventInfo(this.id, dateTime, event))
    }
}

suspend fun HamsterDailyDiff.handleExit(event: HamsterEvent.HamsterExit, dateTime: LocalDateTime) {
    if (this.stopRunningTime < this.lastEventTime) {
        return
    }

    if (this.stopRunningTime > dateTime) {
        this.stopRunningTime = dateTime
    }

    val timeDiff = Duration.between(lastEventTime, this.stopRunningTime).toMillis()
    this.timeDiff += timeDiff
    this.roundsDiff += timeDiff / ServerSettings.CIRCLE_DURATION_MILLIS
}

suspend fun HamsterDailyDiff.handleEnter(event: HamsterEvent.HamsterEnter, dateTime: LocalDateTime) {
    return
}

suspend fun HamsterDailyDiff.handleWheelSpin(event: HamsterEvent.WheelSpin, dateTime: LocalDateTime) {
    if (this.stopRunningTime < this.lastEventTime) {
        this.stopRunningTime = dateTime + Duration.ofMillis(event.durationMs)
        return
    }

    if (this.stopRunningTime < dateTime) {
        val timeDiff = Duration.between(lastEventTime, this.stopRunningTime).toMillis()
        this.timeDiff += timeDiff
        this.roundsDiff += timeDiff / ServerSettings.CIRCLE_DURATION_MILLIS
    }

    this.stopRunningTime = dateTime + Duration.ofMillis(event.durationMs)
}

suspend fun HamsterDailyDiff.handleSensorFailure(event: HamsterEvent.SensorFailure, dateTime: LocalDateTime) {
    return
}

suspend fun HamsterDailyDiff.handleInit(event: HamsterEvent.HamsterSensorInit, dateTime: LocalDateTime) {
    return
}

suspend fun HamsterDailyDiff.handleEvents(events: List<HamsterSensorAggregate.HamsterEventInfo>, dateTime: LocalDateTime) {
    events.forEach {
        when (it.event) {
            is HamsterEvent.HamsterExit -> this.handleExit(it.event, it.dateTime)
            is HamsterEvent.HamsterEnter -> this.handleEnter(it.event, it.dateTime)
            is HamsterEvent.WheelSpin -> this.handleWheelSpin(it.event, it.dateTime)
            is HamsterEvent.SensorFailure -> this.handleSensorFailure(it.event, it.dateTime)
            is HamsterEvent.HamsterSensorInit -> this.handleInit(it.event, it.dateTime)
        }
        this.lastEventTime = it.dateTime
    }
    if (this.stopRunningTime > this.lastEventTime) {
        val stopTime = if (this.stopRunningTime > dateTime) dateTime else this.stopRunningTime
        val timeDiff = Duration.between(lastEventTime, stopTime).toMillis()
        this.timeDiff += timeDiff
        this.roundsDiff += timeDiff / ServerSettings.CIRCLE_DURATION_MILLIS
    }
}