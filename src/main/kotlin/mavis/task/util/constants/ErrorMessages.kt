package mavis.task.util.constants

class ErrorMessages {
    companion object {
        const val UNEXPECTED_EXCEPTION = "Unexpected exception happen"
        const val ERROR_WHILE_PARSING = "Error while parsing %s to type %s"
        const val CANNOT_GENERATE = "Cannot generate %s: %s"
        const val PARAMETER_NOT_PROVIDED = "No %s were provided to process"
        const val NO_VALUE_FOUND = "No needed value for %s event"
        const val HAMSTER_IS_NOT_INSIDE_WHEEL = "Hamster with id %s is not inside wheel"
        const val HAMSTER_IS_INSIDE_WHEEL = "Hamster with id %s is inside wheel"
        const val SENSOR_IS_NOT_WORKING = "Sensor with id %s is already broken to send events"
        const val ERROR_WHILE_PROCESSING = "Error were happen while processing %s event: %s"
        const val DATE_IS_NOT_STARTED_FOR_REPORT = "Date %s has start to generate report"
        const val UNSUPPORTED_EVENT = "Unsupported event happen %s"
        const val ERROR_WHILE_SERIALIZATION = "Cannot serialize object due to: %s"
        const val ERROR_WHILE_ADDING_TO_QUEUE = "Cannot add event to queue due to: %s"
        const val INVALID_SPINNING_TIME = "Invalid spinning time for event %s"
    }
}

