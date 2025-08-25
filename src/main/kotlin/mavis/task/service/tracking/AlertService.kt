package mavis.task.service.tracking

interface AlertService {
    suspend fun sendAlert(message: String)
}