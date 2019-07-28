package pw.binom.builder.server

import pw.binom.builder.common.JobEntity
import pw.binom.builder.common.NodeInfo
import pw.binom.io.IOException
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.json.jsonArray
import pw.binom.logger.Logger
import pw.binom.logger.info

class TasksHandler(
        val taskManager: TaskManager,
        val executeScheduler: ExecuteScheduler,
        val executionControl: ExecutionControl
) : PathHandler() {
    private val LOG = Logger.getLog("/tasks")

    init {
        filter(method("GET")) { r, q ->
            val list = taskManager.getPath(r.contextUri)
                    ?.map {
                        when (it) {
                            is TaskManager.Job -> JobEntity.Job(it.path)
                            is TaskManager.Dir -> JobEntity.Folder(it.path)
                            else -> TODO()
                        }
                    }
                    ?: return@filter
            q.status = 200
            q.resetHeader("Content-Type", "application/json")
            val writer = q.output.utf8Appendable()
            jsonArray(writer) {
                list.forEach {
                    node {
                        it.write(this)
                    }
                }
            }
            q.output.flush()
        }

        filter(method("POST") + endsWith("/execute")) { req, resp ->
            val job = taskManager.getJob(req.contextUri)
            if (job == null) {
                LOG.info("Job ${req.contextUri} not found")
                resp.status = 404
                return@filter
            }

            resp.status = 200
            executeScheduler.execute(job).write(resp.output.utf8Appendable())
            resp.output.flush()
        }
        filter(method("GET") + equal("/wait")) { req, resp ->
            val nodeInfo = NodeInfo.read(req.input.utf8Reader())
            executionControl.idleNode(nodeInfo)

            val jobDescription = executeScheduler.getExecute(nodeInfo.platform, 60_000 * 10)
            if (jobDescription == null) {
                resp.status = 408
                executionControl.reset(nodeInfo)
                return@filter
            }
            try {
                resp.status = 200
                jobDescription.write(resp.output.utf8Appendable())
                resp.output.flush()
                LOG.info("Start job: ${jobDescription.path}:${jobDescription.buildNumber} on ${nodeInfo.toInfo()}")
                executionControl.execute(nodeInfo, jobDescription.toExecuteJob())
            } catch (e: IOException) {
                LOG.info("Node ${nodeInfo.toInfo()} is broken")
                executionControl.reset(nodeInfo)
                executeScheduler.execute(taskManager.getJob(jobDescription.path)!!)
            }
        }
    }

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        super.request(req, resp)
    }
}