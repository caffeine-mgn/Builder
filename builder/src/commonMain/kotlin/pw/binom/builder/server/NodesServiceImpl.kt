package pw.binom.builder.server
/*
import pw.binom.builder.Topic
import pw.binom.builder.node.NODE_TTL
import pw.binom.builder.remote.*
import pw.binom.krpc.Struct
import pw.binom.thread.Thread

class NodesServiceImpl(val eventTopic: Topic<Struct>, val processService: ProcessServiceImpl) : NodesServiceAsync {
    override suspend fun getNodes(): List<NodeStatus> =
            nodes.values.map {
                NodeStatus(
                        description = it.node,
                        process = it.process
                )
            }

    private val nodes = HashMap<String, RemoteNode>()

    class RemoteNode(val node: NodeDescription, var lastTime: Long) {
        var process: JobProcess? = null
    }

    override suspend fun pass(node: NodeDescription, process: JobProcess?): Boolean? {
        var remote = nodes[node.id]
        if (remote == null) {
            remote = RemoteNode(node = node, lastTime = Thread.currentTimeMillis())
            nodes[node.id] = remote
            eventTopic.dispatch(Event_AttachNode(node))
            return null
        } else {
            remote.lastTime = Thread.currentTimeMillis()
        }

        if (remote.process == null && process != null)
            eventTopic.dispatch(Event_ChangeJobNode(node, process))

        if (remote.process != null && process == null)
            eventTopic.dispatch(Event_ChangeJobNode(node, process))

        if (remote.process != null && process != null && (remote.process!!.path != process.path || remote.process!!.buildNumber != process.buildNumber))
            eventTopic.dispatch(Event_ChangeJobNode(node, process))
        remote.process = process

        if (process != null) {
            return processService.getProcess(process).status != JobStatusType.CANCELED
        }
        return null
    }

    fun update() {
        val forRemote = nodes.entries
                .asSequence()
                .filter {
                    Thread.currentTimeMillis() - it.value.lastTime > (NODE_TTL * 1.5).toLong()
                }
                .map { it.key }
                .toList()

        forRemote.forEach {
            val node = nodes.remove(it) ?: return@forEach
            eventTopic.dispatch(Event_DetachNode(node.node))
        }
    }

}
*/