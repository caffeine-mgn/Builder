package pw.binom.builder.master
/*
import pw.binom.builder.remote.*
import pw.binom.builder.server.taskStorage.TaskStorage
import pw.binom.logger.Logger
import pw.binom.logger.info

class TaskManagerServiceImpl(private val taskManager: TaskStorage) : TaskManagerServiceAsync {
    override suspend fun createFolder(path: String, name: String): TaskItem? =
            taskManager.getDir(path)?.createDir(name)?.toTaskItem()

    private val LOG = Logger.getLog("TaskManager")
    override suspend fun createJob(path: String, name: String): TaskItem? {
        val dir = taskManager.getDir(path)
        if (dir == null) {
            LOG.info("Directory \"$path\" not found")
            return null
        }
        return dir.createJob(
                name = name,
                job = JobInformation(
                        cmd = "",
                        exclude = listOf(),
                        include = listOf(),
                        env = listOf()
                )
        ).toTaskItem()
    }

    override suspend fun getLastOutput(process: JobProcess, size: Int): LastOutDto =
            taskManager.getJob(process.path)?.getOutputLast(buildNum = process.buildNumber, length = size)
                    ?: LastOutDto("", 0L)

    override suspend fun getJobBuildings(path: String): List<JobStatus> =
            taskManager.getJob(path)!!.getBuilds()

    override suspend fun updateJob(path: String, jobInformation: JobInformation): JobInformation? =
            taskManager.getJob(path)?.jobFile()?.save(jobInformation)?.toJobInformation()

    override suspend fun getJob(path: String): JobInformation? =
            taskManager.getJob(path)?.jobFile()?.toJobInformation()

    override suspend fun getItems(path: String): List<TaskItem> {
        return taskManager.getPath(path)?.map {
            it.toTaskItem()
        } ?: emptyList()
    }

}
 */