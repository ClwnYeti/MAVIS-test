package mavis.task.util.constants

class LoggingMessages {
    companion object {
        const val STORAGE_RECREATING_STARTED = "Storage %s started changing values due to config editing"
        const val STORAGE_RECREATING_ENDED = "Storage %s ended changing values due to config editing"
        const val NO_VALUE_BY_INDEX = "Storage %s does not have element for %d index"
        const val HAMSTER_NOT_RUNNING = "Hamster with id %s is not running for 180 minutes or has no sensor to watch it"
        const val NO_EVENT_FROM_SENSOR = "Sensor with id %s is not sending events for more than 30 minutes"
        const val PROCESSING_EVENT_START = "Processing event started: %s"
        const val PROCESSING_EVENT_END = "Processing event ended: %s"
        const val CHANGING_CONFIG = "Config is changing, workers were canceled"
    }
}