package pw.binom.builder.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.builder.Event

@Serializable
sealed class Entity {
    abstract val path: String
    val name: String
        get() {
            val i = path.lastIndexOf('/')
            return if (i == -1)
                path
            else
                path.substring(i + 1)
        }

    @SerialName("task")
    @Serializable
    data class Job(
            override var path: String,
            var lastBuildTime: Long?,
            var config: JobConfig,
            val builds: MutableList<Build>
    ) : Entity() {
        @Serializable
        data class Build(
                var worker: Worker?,
                var buildNumber: Int?,
                var status: Event.TaskChangeStatus.JobStatusType
        )
    }

    @Serializable
    class JobConfig(
            val cmd: String,
            val env: Map<String, String>,
            val include: Set<String>,
            val exclude: Set<String>
    )

    @SerialName("dir")
    @Serializable
    class Direction(override val path: String) : Entity()
}