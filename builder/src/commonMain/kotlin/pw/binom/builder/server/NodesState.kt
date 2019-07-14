package pw.binom.builder.server

import pw.binom.builder.common.ExecuteJob
import pw.binom.builder.common.NodeInfo
import pw.binom.builder.common.NodeStatus

class NodesState {
    private val _idels = HashSet<NodeInfo>()
    private val _executions = HashMap<NodeInfo, ExecuteJob>()

    fun idleNode(node: NodeInfo) {
        _idels += node
        _executions.remove(node)
    }

    fun execute(node: NodeInfo, job: ExecuteJob) {
        _idels -= node
        _executions[node] = job
    }

    fun clear(node: NodeInfo) {
        _idels -= node
        _executions.remove(node)
    }

    fun finish(job: ExecuteJob) {
        _executions.entries.find { it.value == job }?.let { _executions.remove(it.key) }
    }

    val status: Set<NodeStatus>
        get() {
            val out = HashSet<NodeStatus>()
            _idels.asSequence().map {
                NodeStatus(it, null)
            }.forEach {
                out += it
            }
            _executions.asSequence().map {
                NodeStatus(it.key, it.value)
            }.forEach {
                out += it
            }
            return out
        }
}