package mavis.task.service.tracking

import kotlinx.coroutines.flow.SharedFlow
import mavis.task.event.Event

interface ActivityTracker {
    val eventsFlow : SharedFlow<Event>
    suspend fun startTracking()
    suspend fun checkDates()
    suspend fun addEvent(event: Event)
}