package pw.binom.builder.server

import pw.binom.builder.common.NodeInfo
import pw.binom.io.*
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.withContextURI
import pw.binom.json.jsonArray

class RootHandler(val taskManager: TaskManager, val executeControl: ExecuteControl, val status: NodesState) : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        println("request ${req.method} ${req.uri}")
        try {
            when {
                req.method == "GET" && req.contextUri.startsWith("/tasks/") -> getTasks(req.withContextURI(req.contextUri.removePrefix("/tasks/")), resp)
                req.method == "POST" && req.contextUri.startsWith("/tasks/") && req.contextUri.endsWith("/execute")
                -> executeTask(req.withContextURI(req.contextUri.removePrefix("/tasks/").removeSuffix("/execute")), resp)
                req.method == "POST" && req.contextUri == "/event" -> waitEvent(req, resp)
                req.method == "POST" && req.contextUri.startsWith("/execution/") && req.contextUri.endsWith("/stdout") ->
                    writeStdOut(req.withContextURI(req.contextUri.removePrefix("/execution").removeSuffix("/stdout")), resp)
                req.method == "POST" && req.contextUri.startsWith("/execution/") && req.contextUri.endsWith("/stderr") ->
                    writeStdErr(req.withContextURI(req.contextUri.removePrefix("/execution").removeSuffix("/stderr")), resp)
                req.method == "POST" && req.contextUri.startsWith("/execution/") && req.contextUri.endsWith("/finish") ->
                    finish(req.withContextURI(req.contextUri.removePrefix("/execution").removeSuffix("/finish")), resp)
                req.method == "GET" && req.contextUri == "/nodes" -> getStates(req, resp)
                else -> {
                    resp.status = 404
                    println("Not found!")
                }
            }
        } catch (e: Throwable) {
            println("Error: $e")
            throw e
        }
    }

    private suspend fun getStates(req: HttpRequest, resp: HttpResponse) {
        resp.status = 200
        resp.resetHeader("Content-Type", "application/json")
        jsonArray(resp.output.utf8Appendable()) {
            status.status.forEach {
                node {
                    it.write(this)
                }
            }
        }
        resp.output.flush()
    }

    private suspend fun finish(req: HttpRequest, resp: HttpResponse) {
        val p = req.contextUri.lastIndexOf('/')
        if (p == -1) {
            resp.status = 404
            return
        }

        val buildNum = req.contextUri.substring(p + 1).toLong()
        val jobPath = req.contextUri.substring(1, p)
        val job = taskManager.getJob(jobPath)
        if (job == null) {
            resp.status = 404
            return
        }
        val txt = req.input.utf8Reader().readText()
        job.finish(buildNum = buildNum, ok = txt == "true")
        resp.resetHeader("Content-Length", "2")
        resp.status = 200
        resp.output.write("OK")
        resp.output.flush()
        println("jobPath=$jobPath")
        println("job.toExecuteJob(buildNum)=${job.toExecuteJob(buildNum)}")
        status.finish(job.toExecuteJob(buildNum))
    }

    private suspend fun writeStdOut(req: HttpRequest, resp: HttpResponse) {
        val p = req.contextUri.lastIndexOf('/')
        if (p == -1) {
            resp.status = 404
            return
        }

        val buildNum = req.contextUri.substring(p + 1)
        val jobPath = req.contextUri.substring(0, p)
        val job = taskManager.getJob(jobPath)
        if (job == null) {
            resp.status = 404
            return
        }
        resp.resetHeader("Content-Length", "2")
        val txt = req.input.utf8Reader().readText()
        job.writeStdout(buildNum = buildNum.toLong(), txt = txt)
        resp.output.write("OK")
        resp.status = 200
    }

    private suspend fun writeStdErr(req: HttpRequest, resp: HttpResponse) {
        val p = req.contextUri.lastIndexOf('/')
        if (p == -1) {
            resp.status = 404
            return
        }

        val buildNum = req.contextUri.substring(p + 1)
        val jobPath = req.contextUri.substring(0, p)
        val job = taskManager.getJob(jobPath)
        if (job == null) {
            resp.status = 404
            return
        }
        job.writeStderr(buildNum = buildNum.toLong(), txt = req.input.utf8Reader().readText())
        resp.resetHeader("Content-Length", "2")
        resp.output.write("OK")
        resp.output.flush()
        resp.status = 200
    }

    private suspend fun waitEvent(req: HttpRequest, resp: HttpResponse) {


        val nodeInfo = NodeInfo.read(req.input.utf8Reader())
        println("Node For Execution: ${nodeInfo.toInfo()}")
        status.idleNode(nodeInfo)

        val ee = executeControl.getExecute(nodeInfo.platform, 30_000)
        if (ee == null) {
            resp.status = 408
            status.clear(nodeInfo)
            return
        }
        try {
            resp.status = 200
            ee.write(resp.output.utf8Appendable())
            resp.output.flush()
            println("Start job: ${ee.path}:${ee.buildNumber} on ${nodeInfo.toInfo()}")
            status.execute(nodeInfo, ee.toExecuteJob())
        } catch (e: IOException) {
            println("Node ${nodeInfo.toInfo()} is broken")
            status.clear(nodeInfo)
            executeControl.execute(taskManager.getJob(ee.path)!!)
        }
    }

    private suspend fun executeTask(req: HttpRequest, resp: HttpResponse) {
        val job = taskManager.getJob(req.contextUri)
        if (job == null) {
            println("Job ${req.contextUri} not found")
            resp.status = 404
            return
        }

        resp.status = 200
        executeControl.execute(job).write(resp.output.utf8Appendable())
        resp.output.flush()
    }

    private suspend fun getTasks(req: HttpRequest, resp: HttpResponse) {
        val list = taskManager.getPath(req.contextUri)
        if (list == null) {
            resp.status = 404
            return
        }
        resp.status = 200
        resp.resetHeader("Content-Type", "application/json")
        val writer = resp.output.utf8Appendable()
        jsonArray(writer) {
            list.forEach {
                node {
                    string("type", if (it is TaskManager.Dir) "dir" else "job")
                    string("name", it.name)
                }
            }
        }
        resp.output.flush()
    }
}