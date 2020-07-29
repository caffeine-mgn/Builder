package pw.binom.builder.master.taskStorage

import kotlinx.serialization.Serializable

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
    }

    @Serializable
    enum class JobStatusType(val terminateState:Boolean) {
        PREPARE(false),
        PROCESS(false),
        FINISHED_OK(true),
        FINISHED_ERROR(true),
        CANCELED(true)
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