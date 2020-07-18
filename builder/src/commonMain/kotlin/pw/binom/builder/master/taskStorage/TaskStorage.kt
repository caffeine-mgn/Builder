package pw.binom.builder.master.taskStorage

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


    data class JobConfig(val cmd: String, val env: Map<String, String>, val include: List<String>, val exclude: List<String>)

    interface Direction : Entity, EntityHolder {

    }

    interface Job : Entity {
        fun getBuild(build: Int): Build?
        fun getBuilds(): List<Build>
        fun createBuild(): Build
    }

    enum class JobStatusType {
        PREPARE,
        PROCESS,
        FINISHED_OK,
        FINISHED_ERROR,
        CANCELED
    }

    interface Build {
        val job: Job
        val number: Int
        val status: BuildStatus
        fun addStdout(text: String)
        fun addStderr(text: String)
    }

    enum class BuildStatus {
        PREPARE,
        PROCESS,
        FINISHED_OK,
        FINISHED_ERROR,
        CANCELED
    }
}