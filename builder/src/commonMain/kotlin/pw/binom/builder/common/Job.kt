package pw.binom.builder.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import pw.binom.builder.node.Client

private val dtoModule33 = SerializersModule {
    this.polymorphic(Job::class, Job::class, Job.serializer())
}

private val jobJsonSerialization = Json {
    classDiscriminator = "@class"
    serializersModule = dtoModule33
}

@Serializable
sealed class Job {
    fun toJson(): String = jobJsonSerialization.encodeToString(serializer(), this)

    companion object {
        fun toJob(json: String): Job = jobJsonSerialization.decodeFromString(serializer(), json)
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