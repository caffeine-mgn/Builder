package pw.binom.builder.web

import pw.binom.builder.server.*
import pw.binom.io.utf8Appendable
/*
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
*/