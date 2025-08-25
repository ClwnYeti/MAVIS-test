package mavis.task.storage

import kotlinx.coroutines.sync.Mutex

interface Storage<T> {
    suspend fun getAll(): List<T>
    suspend fun getByIndex(index: Int): T?
    suspend fun getById(id: String): T?
    suspend fun getByIdOrThrow(id: String): T
    suspend fun recreate(newList: List<T>)
}