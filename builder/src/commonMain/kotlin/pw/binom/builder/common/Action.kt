package pw.binom.builder.common

import pw.binom.io.AsyncAppendable
import pw.binom.io.AsyncReader
import pw.binom.json.*

/**
 * Действия, который должен проделать сборочный узел
 */
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
            val type = node.obj["type"]!!.text
            return when (type) {
                "cancel" -> Cancel
                "clearBuilds" -> ClearBuilds
                else -> TODO()
            }
        }
    }
}