package pw.binom.builder.server.taskStorage

interface EntityHolder {
    fun getEntity(path: String): TaskStorage.Entity?
    fun getEntityList(): List<TaskStorage.Entity>?
    fun createJob(name:String, config: TaskStorage.JobConfig)
    fun createDirection(name: String)
}