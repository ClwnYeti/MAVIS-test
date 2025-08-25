package mavis.task.repository

import java.time.LocalDate

interface Repository<T> {
    suspend fun add(value: T)
    suspend fun findByDate(date: LocalDate): List<T>
    suspend fun getNewId(): Int
    suspend fun clear()
}