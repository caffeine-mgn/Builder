package pw.binom.builder

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import pw.binom.builder.dto.Worker

@OptIn(ImplicitReflectionSerializer::class)
private val dtoModule22 = SerializersModule {
    this.polymorphic(Event::class.serializer())
}

private val eventJsonSerialization = Json(JsonConfiguration.Stable.copy(
        classDiscriminator = "@t"
), dtoModule22)

@Serializable
sealed class Event {

    fun toJson(): String = eventJsonSerialization.stringify(serializer(), this)

    companion object {
        fun toEvent(json: String): Event = eventJsonSerialization.parse(serializer(), json)
    }

    @SerialName("add_node")
    @Serializable
    class AddNode(val id: String, val name: String, val tags: Set<String>) : Event()

    @SerialName("delete_node")
    @Serializable
    class DeleteNode(val id: String) : Event()

    @SerialName("node_change_status")
    @Serializable
    class NodeChangeStatus(val slaveId: String, val status: SlaveStatus?) : Event() {
        @Serializable
        class SlaveStatus(val jobPath: String, val buildNumber: Int)
    }


    @SerialName("task_change_status")
    @Serializable
    data class TaskChangeStatus(val path: String, val worker: Worker?, val buildNumber: Int?, val status: JobStatusType) : Event() {
        @Serializable
        enum class JobStatusType(val terminateState: Boolean) {
            PREPARE(false),
            PROCESS(false),
            FINISHED_OK(true),
            FINISHED_ERROR(true),
            CANCELED(true)
        }
    }
}