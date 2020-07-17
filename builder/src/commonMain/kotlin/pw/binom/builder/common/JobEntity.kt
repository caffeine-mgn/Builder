package pw.binom.builder.common
/*
import pw.binom.io.AsyncAppendable
import pw.binom.io.AsyncReader
import pw.binom.json.*

sealed class JobEntity(val path: String) {
    class Job(path: String) : JobEntity(path)
    class Folder(path: String) : JobEntity(path)

    val name: String
        get() {
            val p = path.lastIndexOf('/')
            return if (p == -1)
                path
            else
                path.substring(p)
        }

//    suspend fun write(appendable: AsyncAppendable) {
//        jsonNode(appendable) {
//            write(this)
//        }
//    }

    suspend fun write(): JsonNode =
            jsonNode {
                string("path", path)
                when (this@JobEntity) {
                    is Job -> string("type", "job")
                    is Folder -> string("type", "folder")
                }
            }

    suspend fun write(ctx: ObjectCtx) {
        ctx.run {
            string("path", path)
            when (this@JobEntity) {
                is Job -> string("type", "job")
                is Folder -> string("type", "folder")
            }
        }
    }

    companion object {
        suspend fun read(reader: AsyncReader): JobEntity {
            val r = JsonDomReader()
            JsonReader(reader).accept(r)
            return read(r.node)
        }

        fun read(node: JsonNode): JobEntity {
            val path = node.obj["path"]!!.string
            val type = node.obj["type"]!!.string
            return when (type) {
                "job" -> Job(path)
                "folder" -> Folder(path)
                else -> TODO()
            }
        }
    }
}
*/