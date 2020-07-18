package pw.binom.builder.common

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import pw.binom.builder.node.Client
import pw.binom.builder.server.Master

@OptIn(ImplicitReflectionSerializer::class)
private val dtoModule = SerializersModule {
    this.polymorphic(Action::class.serializer())
}

private val actionJsonSerialization = Json(JsonConfiguration.Stable.copy(
        classDiscriminator = "@class"
), dtoModule)

@Serializable
sealed class Action {

    fun toJson() = actionJsonSerialization.stringify(serializer(), this)

    companion object {
        fun toAction(json: String): Action = actionJsonSerialization.parse(serializer(), json)
    }

    open suspend fun executeSlave(client: Client): Action? = null
    open suspend fun executeMaster(master: Master): Action? = null

    @Serializable
    class NodePing(val id: String) : Action(){
        override suspend fun executeMaster(master: Master): Action? {
            println("Execute on Master!")
            return super.executeMaster(master)
        }
    }

    @Serializable
    class NodeConnect(val name: String, val id: String, val tags: List<String>) : Action() {
    }

    @Serializable
    class NOP : Action()
}

/**
 * Действия, который должен проделать сборочный узел
 */
/*
sealed class Action {
    object Cancel : Action()
    object ClearBuilds : Action()

    suspend fun write(appendable: AsyncAppendable) {
        jsonNode(appendable) {
            write(this)
        }
    }

    suspend fun write(ctx: ObjectCtx) {
        ctx.run {
            when (this@Action) {
                is Cancel -> string("type", "cancel")
                is ClearBuilds -> string("type", "clearBuilds")
            }
        }
    }

    companion object {
        suspend fun read(reader: AsyncReader): Action {
            val r = JsonDomReader()
            JsonReader(reader).accept(r)
            return read(r.node)
        }

        fun read(node: JsonNode): Action {
            val type = node.obj["type"]!!.string
            return when (type) {
                "cancel" -> Cancel
                "clearBuilds" -> ClearBuilds
                else -> TODO()
            }
        }
    }
}
*/