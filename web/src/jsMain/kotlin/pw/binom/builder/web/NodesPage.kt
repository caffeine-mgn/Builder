package pw.binom.builder.web

import pw.binom.builder.remote.*
import pw.binom.io.Closeable
import kotlin.dom.addClass

class NodesPage : Page() {
    override suspend fun getTitle(): String = "Nodes"

    private val list = ListView<NodeItem>().appendTo(layout)


    private inner class NodeItem(val node: NodeDescription, job: JobProcess?) : DivComponentWithLayout() {
        var job: JobProcess? = job
            set(value) {
                field = value
                refresh()
            }

        private val nodeId = Span(node.id).appendTo(layout, grow = 1, shrink = 1, basis = 0)
        private val jobSpan = Span().appendTo(layout, grow = 1, shrink = 1, basis = 0)

        private fun refresh() {
            jobSpan.text = job?.asShort ?: "No Task"
        }

        init {
            nodeId.dom.addClass(Styles.SIMPLE_TEXT)
            jobSpan.dom.addClass(Styles.SIMPLE_TEXT)
            refresh()
        }
    }

    override suspend fun onInit() {
        super.onInit()
        Client.nodesService.getNodes().map {
            NodeItem(node = it.description, job = it.process)
        }.forEach {
            list.addLast(it)
        }
    }

    private var listener: Closeable? = null

    private fun findNodeById(id: String) = list.asSequence().find { it.node.id == id }

    override suspend fun onStart() {
        super.onStart()
        listener = EventBus.wait {
            when (it) {
                is Event_AttachNode -> {
                    if (findNodeById(it.node.id) == null)
                        list.addLast(NodeItem(node = it.node, job = null))
                }
                is Event_DetachNode -> {
                    findNodeById(it.node.id)
                            ?.let {
                                list.remove(it)
                            }
                }
                is Event_ChangeJobNode -> {
                    console.info("Change job state!  it.node.id=${it.node.id}")
                    console.info("Nodes:")
                    list.asSequence().forEach {
                        console.info("->${it.node.id}")
                    }
                    val node = findNodeById(it.node.id)
                    node?.let { node ->
                        node.job = it.job
                        console.info("Done!")
                    }
                    if (node == null) {
                        console.info("Can't find node ${it.node.id}")
                    }
                }
            }
        }
    }

    override suspend fun onStop() {
        listener?.close()
        super.onStop()
    }
}