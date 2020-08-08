package pw.binom.builder.master.taskStorage

import kotlinx.serialization.Serializable
import pw.binom.builder.Event

interface TaskStorage : EntityHolder {
    interface Entity {
        val path: String
        val name: String
            get() {
                val i = path.lastIndexOf('/')
                return if (i == -1)
                    path
                else
                    path.substring(i + 1)
            }

        fun delete()
    }


    @Serializable
    data class JobConfig(val cmd: String, val env: Map<String, String>, val include: Set<String>, val exclude: Set<String>)

    interface Direction : Entity, EntityHolder {

    }

    interface Job : Entity {
        fun getBuild(build: Int): Build?
        fun getBuilds(): List<Build>
        fun createBuild(): Build
        val config: JobConfig
        fun update(config: JobConfig)
        fun updateLastBuildTime(time: Long)
        val lastBuildTime: Long?
    }

    @Serializable
    enum class JobStatusType(val terminateState: Boolean) {
        PREPARE(false),
        PROCESS(false),
        FINISHED_OK(true),
        FINISHED_ERROR(true),
        CANCELED(true);

        fun toDTO()=
                when (this) {
                    PREPARE -> Event.TaskChangeStatus.JobStatusType.PREPARE
                    PROCESS -> Event.TaskChangeStatus.JobStatusType.PROCESS
                    FINISHED_OK -> Event.TaskChangeStatus.JobStatusType.FINISHED_OK
                    FINISHED_ERROR -> Event.TaskChangeStatus.JobStatusType.FINISHED_ERROR
                    CANCELED -> Event.TaskChangeStatus.JobStatusType.CANCELED
                }
    }

    interface Build {
        val job: Job
        val number: Int
        var status: JobStatusType
        fun addStdout(text: String)
        fun addStderr(text: String)
    }
//
//    enum class BuildStatus {
//        PREPARE,
//        PROCESS,
//        FINISHED_OK,
//        FINISHED_ERROR,
//        CANCELED
//    }
}