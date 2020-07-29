package pw.binom.builder.master.taskStorage.file

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

@Serializable
class JobDto(val cmd: String, val env: Map<String, String>, val include: Set<String>, val exclude: Set<String>, var nextBuild: Int)

@Serializable
class BuildDto(var status: BuildStatus) {
    enum class BuildStatus {
        PREPARE,
        PROCESS,
        FINISHED_OK,
        FINISHED_ERROR,
        CANCELED
    }
}

@OptIn(ImplicitReflectionSerializer::class)
private val dtoModule = SerializersModule {
    this.polymorphic(JobDto::class.serializer())
    this.polymorphic(BuildDto::class.serializer())
}

val taskStorageJsonSerialization = Json(JsonConfiguration.Stable.copy(
        classDiscriminator = "@class"
), dtoModule)