package pw.binom.builder.common
/*
import pw.binom.Platform
import pw.binom.io.AsyncAppendable
import pw.binom.io.AsyncReader
import pw.binom.json.*

class NodeInfo(val id: String, val platform: Platform, val dataCenter: String) {

    suspend fun write(ctx:ObjectCtx){
        ctx.run {
            string("id", id)
            string("platform", platform.name)
            string("dataCenter", dataCenter)
        }
    }

    suspend fun write(appendable: AsyncAppendable) {
        jsonNode(appendable) {
            write(this)
        }
    }

    fun toInfo() = "Node (#$id): platform=${platform.name} dataCenter=$dataCenter"

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        val node = other as? NodeInfo ?: return false
        return node.id == id
    }

    companion object {
        suspend fun read(reader: AsyncReader): NodeInfo {
            val r = JsonDomReader()
            JsonReader(reader).accept(r)
            return read(r.node)
        }

        fun read(node:JsonNode):NodeInfo{
            val id = node.obj["id"]!!.text
            val platform = node.obj["platform"]!!.text
            val dataCenter = node.obj["dataCenter"]!!.text
            return NodeInfo(
                    id = id,
                    platform = Platform.valueOf(platform),
                    dataCenter = dataCenter
            )
        }
    }
}
*/