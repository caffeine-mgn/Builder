package pw.binom.builder.server

import pw.binom.io.utf8Appendable
import pw.binom.json.jsonArray

class NodesHandler(private val executionControl: ExecutionControl) : PathHandler() {
    init {
        filter(method("GET") + equal("/status")) { r, q ->
            q.status = 200
            q.resetHeader("Content-Type", "application/json")
            jsonArray(q.output.utf8Appendable()) {
                executionControl.status.forEach {
                    node {
                        it.write(this)
                    }
                }
            }
            q.output.flush()
        }
    }
}