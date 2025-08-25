package mavis.task.service.tracking

import kotlinx.coroutines.CoroutineScope
import mavis.task.event.Event

interface EventProcessor {
    suspend fun startProcessing()
    suspend fun processEvent(event: Event)
}