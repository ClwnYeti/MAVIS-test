package mavis.task.util.extensions

import java.time.LocalDate
import java.time.LocalDateTime

fun LocalDate.nextDateStart(): LocalDateTime {
    return this.plusDays(1).atStartOfDay()
}

fun LocalDateTime.nextDateStart(): LocalDateTime {
    return this.plusDays(1)
}