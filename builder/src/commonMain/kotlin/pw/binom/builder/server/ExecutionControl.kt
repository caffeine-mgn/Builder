package pw.binom.builder.server

import pw.binom.Queue
import pw.binom.Stack
import pw.binom.builder.OutType
import pw.binom.builder.Topic
import pw.binom.builder.common.Action
import pw.binom.builder.common.ExecuteJob
import pw.binom.builder.common.NodeInfo
import pw.binom.builder.common.NodeStatus
import pw.binom.io.Closeable

class ExecutionControl(val taskManager: TaskManager) {
    private val nodes = HashSet<NodeInfo>()

    class Out(val type: OutType, val text: String)

    inner class Execution(val job: TaskManager.Job, val node: NodeInfo, val execution: ExecuteJob) : Closeable {

        private var _finished = false
        private var _successful = false
        private var _canceled = false

        private val _actions = Stack<Action>().asFiFoQueue()
        val actions: Queue<Action>
            get() = _actions

        val finished
            get() = _finished

        val successful
            get() = _successful


        val topicOut = Topic<Out>()

        override fun close() {
            topicOut.close()
        }

        fun stdout(txt: String) {
            if (!finished) {
                topicOut.dispatch(Out(OutType.STDOUT, txt))
                job.writeStdout(execution.buildNumber, txt)
            }
        }

        fun stderr(txt: String) {
            if (!finished) {
                topicOut.dispatch(Out(OutType.STDERR, txt))
                job.writeStderr(execution.buildNumber, txt)
            }
        }

        fun finish(successful: Boolean) {
            job.finish(execution.buildNumber, successful)
            _finished = true
            _successful = successful
            close()
        }

        fun addActionCancel() {
            _actions.push(Action.Cancel)
        }

        fun cancel() {
            job.cancel(execution.buildNumber)
            _canceled = true
            _finished = true
            _successful = false
            close()
        }
    }

    private val _executions = HashSet<Execution>()
    val executions: Set<Execution>
        get() = _executions

    fun getExecution(execution: ExecuteJob) = _executions.find { it.execution == execution }

    fun idleNode(node: NodeInfo) {
        nodes += node
        val exe = _executions.filter { it.node == node }
        exe.forEach {
            if (!it.finished)
                it.cancel()
        }
        _executions -= exe
    }

    fun execute(node: NodeInfo, job: ExecuteJob) {
        _executions += Execution(
                job = taskManager.getJob(job.path)!!,
                execution = job,
                node = node
        )
        nodes += node
    }

    fun reset(node: NodeInfo) {
        nodes -= node
        val exe = _executions.filter { it.node == node }
        exe.forEach {
            if (!it.finished)
                it.cancel()
        }
        _executions -= exe
    }

    val status: Set<NodeStatus>
        get() =
            nodes.asSequence().map { node ->
                node to _executions.find { it.node == node }
            }.map {
                NodeStatus(it.first, it.second?.execution)
            }.toSet()
}