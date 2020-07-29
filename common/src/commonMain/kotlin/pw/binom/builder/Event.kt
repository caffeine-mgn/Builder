package pw.binom.builder

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

@OptIn(ImplicitReflectionSerializer::class)
private val dtoModule22 = SerializersModule {
    this.polymorphic(Event::class.serializer())
}

private val eventJsonSerialization = Json(JsonConfiguration.Stable.copy(
        classDiscriminator = "@class"
), dtoModule22)

@Serializable
sealed class Event {

    fun toJson(): String = eventJsonSerialization.stringify(serializer(), this)

    companion object {
        fun toEvent(json: String): Event = eventJsonSerialization.parse(serializer(), json)
    }

    @Serializable
    class AddNode(val id: String, val name: String, val tags: Set<String>) : Event()

    @Serializable
    class DeleteNode(val id: String) : Event()

    @Serializable
    class NodeChangeStatus(val slaveId: String, val status: SlaveStatus?) : Event()

    @Serializable
    class SlaveStatus(val jobPath: String, val buildNumber: Int)
}