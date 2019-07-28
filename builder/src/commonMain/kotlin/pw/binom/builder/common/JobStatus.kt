package pw.binom.builder.common

import pw.binom.json.JsonNode
import pw.binom.json.ObjectCtx
import pw.binom.json.isNull
import pw.binom.json.obj

class JobStatus(val job: ExecuteJob, val node: NodeInfo?) {
    suspend fun write(ctx: ObjectCtx) {
        ctx.run {
            node("job") {
                job.write(this)
            }

            if (node == null)
                attrNull("node")
            else
                node("node") {
                    node.write(this)
                }
        }
    }

    companion object {
        fun read(node: JsonNode): JobStatus =
                JobStatus(
                        job = ExecuteJob.read(node.obj["job"]!!),
                        node = node.obj["node"]?.takeIf { !it.isNull }?.let { NodeInfo.read(it) }
                )
    }
}