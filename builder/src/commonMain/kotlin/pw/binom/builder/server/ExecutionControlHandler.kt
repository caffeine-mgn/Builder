package pw.binom.builder.server

import pw.binom.builder.OutType
import pw.binom.builder.common.ExecuteJob
import pw.binom.builder.common.JobStatus
import pw.binom.io.*
import pw.binom.io.httpServer.HttpRequest
import pw.binom.json.jsonArray
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.logger.warn

class ExecutionControlHandler(
        val executionControl: ExecutionControl,
        val executeScheduler: ExecuteScheduler,
        val taskManager: TaskManager
) : PathHandler() {
    private val LOG = Logger.getLog("/execution")

    init {
        fun readExecuteJob(req: HttpRequest): ExecuteJob {
            val items = req.contextUri.removePrefix("/").split('/')
            return ExecuteJob(buildNumber = items[1].toLong(), path = items[0])
        }

        fun getExecution(req: HttpRequest): ExecutionControl.Execution? {
            return executionControl.getExecution(readExecuteJob(req))
        }

        filter(method("POST") + endsWith("/stdout")) { r, q ->
            val e = getExecution(r) ?: return@filter
            e.stdout(r.input.utf8Reader().readText())
            q.status = 200
        }

        filter(method("POST") + endsWith("/stderr")) { r, q ->
            val e = getExecution(r) ?: return@filter
            e.stderr(r.input.utf8Reader().readText())
            q.status = 200
        }

        filter(method("POST") + endsWith("/finish")) { r, q ->
            val e = getExecution(r) ?: return@filter
            e.finish(r.input.utf8Reader().readText() == "true")
            q.status = 200
        }

        filter(method("POST") + endsWith("/cancel")) { r, q ->
            val e = getExecution(r)
            if (e != null) {
                q.status = 200
                e.addActionCancel()
                return@filter
            }
            if (executeScheduler.cancel(readExecuteJob(r))) {
                q.status = 200
            }
        }

        filter(method("GET") + endsWith("/actions")) { r, q ->
            val e = getExecution(r) ?: return@filter
            var count = 0
            q.status = 200
            jsonArray(q.output.utf8Appendable()) {
                while (!e.actions.isEmpty) {
                    node {
                        e.actions.pop().write(this)
                        count++
                    }
                }
            }
            println("Return events: $count")
            q.output.flush()
        }

        filter(method("GET") + endsWith("/join")) { r, q ->
            val e = getExecution(r) ?: return@filter
            q.status = 200
            try {
                while (true) {
                    try {
                        e.topicOut.wait()
                    } catch (e: ClosedException) {
                        break
                    }
                }
                if (e.successful)
                    q.output.utf8Appendable().append("OK")
                else
                    q.output.utf8Appendable().append("BAD")
            } catch (e: IOException) {
                //NOP
            }
        }

        filter(method("GET") + equal("/")) { r, q ->
            q.status = 200
            jsonArray(q.output.utf8Appendable()) {
                executionControl.executions.forEach {
                    node {
                        JobStatus(job = it.execution, node = it.node).write(this)
                    }
                }

                executeScheduler.tasks.forEach {
                    node {
                        JobStatus(job = it.toExecuteJob(), node = null).write(this)
                    }
                }
            }
        }

        filter(method("GET") + endsWith("/tail")) { r, q ->
            val e = getExecution(r)

            if (e == null) {
                val jobDescription = readExecuteJob(r)
                val job = taskManager.getJob(jobDescription.path)
                if (job == null)
                    LOG.warn("Job ${r.contextUri} not found")
                else {
                    LOG.info("Work was done!")
                    if (job.getStarted(jobDescription.buildNumber) != null)
                        q.status = 200
                    else
                        q.status = 404
                }
                return@filter
            }
            q.status = 200
            val appendable = q.output.utf8Appendable()
            try {
                while (true) {
                    try {
                        val out = e.topicOut.wait()
                        when (out.type) {
                            OutType.STDOUT -> appendable.append("STDOUT:")
                            OutType.STDERR -> appendable.append("STDERR:")
                        }
                        appendable.append(out.text).append("\n")
                        q.output.flush()
                    } catch (e: ClosedException) {
                        break
                    }
                }
            } catch (e: IOException) {
                //NOP
            }
        }
    }
}