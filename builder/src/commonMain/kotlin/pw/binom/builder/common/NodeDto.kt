package pw.binom.builder.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import pw.binom.builder.master.taskStorage.TaskStorage

private val dtoModule11 = SerializersModule {
    this.contextual(NodeDto::class, NodeDto.serializer())
}

private val masterJsonSerialization = Json {
    classDiscriminator = "@class"
    serializersModule = dtoModule11
}

@Serializable
sealed class NodeDto {
    companion object {
        fun toDto(json: String): NodeDto = masterJsonSerialization.decodeFromString(serializer(), json)
    }

    fun toJson(): String = masterJsonSerialization.encodeToString(serializer(), this)

    @Serializable
    data class ChangeState(val buildNumber: Int, val path: String, val status: TaskStorage.JobStatusType) : NodeDto()

    @Serializable
    data class Log(val err: Boolean, val text: String) : NodeDto()
}