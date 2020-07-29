package pw.binom.builder.common

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import pw.binom.builder.node.Client

@OptIn(ImplicitReflectionSerializer::class)
private val dtoModule33 = SerializersModule {
    this.polymorphic(Action::class.serializer())
}

private val jobJsonSerialization = Json(JsonConfiguration.Stable.copy(
        classDiscriminator = "@class"
), dtoModule33)

@Serializable
sealed class Job {
    fun toJson(): String = jobJsonSerialization.stringify(serializer(), this)

    companion object {
        fun toJob(json: String): Job = jobJsonSerialization.parse(serializer(), json)
    }

    @Serializable
    class ExecuteTask(val cmd: String, val env: Map<String, String>, val path: String, val buildNumber: Int) : Job() {
        override suspend fun execute(client: Client) {
            client.startTask(
                    cmd = cmd,
                    buildNumber = buildNumber,
                    env = env,
                    path = path
            )
        }
    }

    suspend open fun execute(client: Client) {}
}