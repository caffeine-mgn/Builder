package pw.binom.builder.common

import pw.binom.io.AsyncAppendable
import pw.binom.io.AsyncReader
import pw.binom.json.*
/*
data class ExecuteJob(val buildNumber: Long, val path: String) {

    suspend fun write(ctx: ObjectCtx) {
        ctx.run {
            number("buildNumber", buildNumber)
            string("path", path)
        }
    }

    suspend fun write() =
            jsonNode {
                number("buildNumber", buildNumber)
                string("path", path)
            }

    suspend fun write(appendable: AsyncAppendable) {
        jsonNode(appendable) {
            write(this)
        }
    }


    override fun toString(): String =
            "$path:$buildNumber"

    companion object {
        suspend fun read(reader: AsyncReader): ExecuteJob {
            val r = JsonDomReader()
            JsonReader(reader).accept(r)
            return read(r.node)
        }

        fun read(node: JsonNode): ExecuteJob {
            val buildNumber = node.obj["buildNumber"]!!.long
            val path = node.obj["path"]!!.text
            return ExecuteJob(buildNumber = buildNumber, path = path)
        }
    }
}

 */