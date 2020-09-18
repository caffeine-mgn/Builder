package pw.binom.builder.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import pw.binom.builder.master.taskStorage.TaskStorage

private val dtoModule11 = SerializersModule {
    this.contextual(MasterDto::class, MasterDto.serializer())
}

private val masterJsonSerialization = Json {
    classDiscriminator = "@class"
    serializersModule = dtoModule11
}

@Serializable
sealed class MasterDto {

    companion object {
        fun toDto(json: String): MasterDto = masterJsonSerialization.decodeFromString(serializer(), json)
    }

    fun toJson(): String = masterJsonSerialization.encodeToString(serializer(), this)

    @Serializable
    data class StartBuild(
            val config: TaskStorage.JobConfig,
            val path: String,
            val buildNumber: Int
    ) : MasterDto()
}