package pw.binom.builder.master.taskStorage.file

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

@Serializable
class JobDto(
        var cmd: String,
        val env: MutableMap<String, String>,
        val include: MutableSet<String>,
        val exclude: MutableSet<String>,
        var nextBuild: Int,
        var lastBuildTime: Long? = null
)

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