package mavis.task.storage

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mavis.task.state.HamsterSensorAggregate
import mavis.task.util.constants.ErrorMessages
import mavis.task.util.constants.LoggingMessages
import mavis.task.util.exception.EventLogicException
import mavis.task.util.exception.StorageException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Repository
import java.lang.String.format

@Repository
class HamsterSensorAggregateStorage: Storage<HamsterSensorAggregate> {
    private val map: MutableMap<String, HamsterSensorAggregate> = HashMap()
    private val log = LoggerFactory.getLogger(HamsterSensorAggregateStorage::class.java)
    private val mutex: Mutex = Mutex()

    override suspend fun getAll(): List<HamsterSensorAggregate> {
        return ArrayList(map.values)
    }

    override suspend fun getByIndex(index: Int): HamsterSensorAggregate? {
        if (index >= map.size) {
            log.warn(format(LoggingMessages.NO_VALUE_BY_INDEX, this::class.simpleName, index))
            return null
        }
        return map.values.elementAt(index)
    }

    override suspend fun getById(id: String): HamsterSensorAggregate? {
        return map[id]
    }

    override suspend fun getByIdOrThrow(id: String): HamsterSensorAggregate {
        val value = getById(id)

        if (value == null) {
            val formattedMessage = format(ErrorMessages.NO_VALUE_FOUND, id)
            log.error(formattedMessage)
            throw StorageException(formattedMessage, )
        }
        return value
    }

    override suspend fun recreate(newList: List<HamsterSensorAggregate>) {
        log.warn(format(LoggingMessages.STORAGE_RECREATING_STARTED, this::class.simpleName))
        mutex.withLock {
            map.clear()
            map.putAll(newList.associateBy { hsa -> hsa.id })
        }

        log.warn(format(LoggingMessages.STORAGE_RECREATING_ENDED, this::class.simpleName))
    }
}