package pw.binom.builder.master.taskStorage.file

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

private val dtoModule = SerializersModule {
    this.polymorphic(JobDto::class, JobDto::class, JobDto.serializer())
    this.polymorphic(BuildDto::class, BuildDto::class, BuildDto.serializer())
}

val taskStorageJsonSerialization = Json {
    classDiscriminator = "@class"
    serializersModule = dtoModule
}