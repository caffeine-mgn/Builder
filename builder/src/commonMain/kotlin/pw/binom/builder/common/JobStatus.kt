package pw.binom.builder.common
/*
import pw.binom.builder.server.Status
import pw.binom.json.*

class JobStatus(val job: ExecuteJob, val start: Long?, val end: Long?, val node: NodeInfo?, val status: Status.Type?) {
    suspend fun write(ctx: ObjectCtx) {
        ctx.run {
            node("job") {
                job.write(this)
            }

            if (start == null)
                attrNull("start")
            else
                number("start", start)

            if (end == null)
                attrNull("end")
            else
                number("end", end)

            if (node == null)
                nil("node")
            else
                node("node") {
                    node.write(this)
                }

            if (status == null)
                attrNull("status")
            else
                string("status", status.name)
        }
    }

    suspend fun write():JsonObject =
            jsonNode {
                node("job") {
                    job.write(this)
                }

                if (start == null)
                    attrNull("start")
                else
                    number("start", start)

                if (end == null)
                    attrNull("end")
                else
                    number("end", end)

                if (node == null)
                    attrNull("node")
                else
                    node("node") {
                        node.write(this)
                    }

                if (status == null)
                    attrNull("status")
                else
                    string("status", status.name)
            }

    companion object {
        fun read(node: JsonNode): JobStatus =
                JobStatus(
                        job = ExecuteJob.read(node.obj["job"]!!),
                        node = node.obj["node"]?.let { NodeInfo.read(it) },
                        start = node.obj["start"]?.long,
                        end = node.obj["end"]?.long,
                        status = node.obj["status"]?.text?.let { Status.Type.valueOf(it) }
                )
    }
}
 */