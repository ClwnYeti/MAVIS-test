package mavis.task.repository

import mavis.task.entity.AlertEntity
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AlertRepository: InMemoryRepository<AlertEntity> {

    override val list: MutableList<AlertEntity> = ArrayList()

    override suspend fun findByDate(date: LocalDate): List<AlertEntity> {
        return list.filter { it -> it.dateTime.toLocalDate() == date }
    }
}