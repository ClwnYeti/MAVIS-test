package mavis.task.service.tracking

import mavis.task.entity.AlertEntity
import mavis.task.repository.Repository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RepositoryAlertService(
    private val repository: Repository<AlertEntity>
): AlertService {
    override suspend fun sendAlert(message: String) {
        repository.add(AlertEntity(repository.getNewId(), message, LocalDateTime.now()))
    }
}