package pw.binom.builder.server

import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.logger.Logger
import pw.binom.logger.info

class RootHandler(taskManager: TaskManager, executeScheduler: ExecuteScheduler, executionControl: ExecutionControl) : PathHandler() {
    private val LOG = Logger.getLog("/")
    private val executionControlHandler = ExecutionControlHandler(executionControl, executeScheduler, taskManager)
    private val tasksHandler = TasksHandler(taskManager, executeScheduler, executionControl)
    private val nodesHandler = NodesHandler(executionControl)

    init {
        filter(startsWith("/execution"), executionControlHandler)
        filter(startsWith("/tasks"), tasksHandler)
        filter(startsWith("/nodes"), nodesHandler)
    }

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        super.request(req, resp)
    }
}