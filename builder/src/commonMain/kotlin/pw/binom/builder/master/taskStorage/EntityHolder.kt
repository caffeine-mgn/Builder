package pw.binom.builder.master.taskStorage

interface EntityHolder {
    fun getEntity(path: String): TaskStorage.Entity?
    fun getEntityList(): List<TaskStorage.Entity>?
    fun createJob(name: String, config: TaskStorage.JobConfig): TaskStorage.Job
    fun createDirection(name: String): TaskStorage.Direction
}

fun EntityHolder.findEntity(path: String): TaskStorage.Entity? {
    val v = path.splitToSequence('/').iterator()
    if (!v.hasNext()) {
        return null
    }
    val first = v.next()
    var entity: TaskStorage.Entity = getEntity(first) ?: return null
    v.forEach {
        if (entity is TaskStorage.Direction) {
            entity = (entity as TaskStorage.Direction).getEntity(it) ?: return null
        } else {
            return null
        }
    }
    return entity
//    var entity: TaskStorage.Entity = this
//    path.splitToSequence('/').forEach {
//        entity = entity.getEntity(it) ?: return null
//    }
}