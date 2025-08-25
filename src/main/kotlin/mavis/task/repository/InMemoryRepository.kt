package mavis.task.repository

interface InMemoryRepository<T>: Repository<T> {
    val list: MutableList<T>
    override suspend fun add(value: T) {
        list.add(value)
    }

    override suspend fun getNewId(): Int {
        return list.size
    }

    override suspend fun clear() {
        list.clear()
    }
}