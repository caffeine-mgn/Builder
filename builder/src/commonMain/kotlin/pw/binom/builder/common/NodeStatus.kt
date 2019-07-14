package pw.binom.builder.common

import pw.binom.io.AsyncAppendable
import pw.binom.io.AsyncReader
import pw.binom.json.*

class NodeStatus(val node: NodeInfo, val job: ExecuteJob?) {

    suspend fun write(ctx: ObjectCtx) {
        ctx.run {
            node("node") {
                node.write(this)
            }
            if (job == null)
                attrNull("job")
            else
                node("job") {
                    job.write(this)
                }
        }
    }

    suspend fun write(appendable: AsyncAppendable) {
        jsonNode(appendable) {
            write(this)
        }
    }

    companion object {
        fun read(node: JsonNode): NodeStatus {
            val nodeInfo = NodeInfo.read(node.obj["node"]!!)
            val job = node.obj["job"]?.takeIf { !it.isNull }?.let { ExecuteJob.read(it) }
            return NodeStatus(
                    node = nodeInfo,
                    job = job
            )
        }
        suspend fun read(reader: AsyncReader): NodeStatus {
            val r = JsonDomReader()
            JsonReader(reader).accept(r)
            return read(r.node)
        }
    }
}