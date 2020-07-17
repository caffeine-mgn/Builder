package pw.binom.builder.web

import pw.binom.builder.Topic
import pw.binom.builder.remote.*
import pw.binom.builder.server.Out
import pw.binom.builder.server.ProcessServiceImpl
import pw.binom.builder.server.TaskManager1
import pw.binom.builder.server.contextUriWithoutParams
import pw.binom.io.*
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.json.obj
import pw.binom.json.parseJSON
import pw.binom.json.write
import pw.binom.krpc.RPCService
import pw.binom.krpc.Struct
import pw.binom.krpc.StructFactory
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.logger.warn
import pw.binom.rpc.JsonRpc
import pw.binom.stackTrace


fun RPCService<*, *>.findMethod(name: String) = methods.find { it.name == name }

class ApiHandler(val process: ProcessServiceImpl,
                 val taskManager: TaskManagerServiceAsync,
                 val tt: TaskManager1,
                 val eventTopic: Topic<Struct>,
                 val nodesService: NodesServiceAsync) : Handler {

    private val LOG = Logger.getLog("API")

    private suspend fun tail(req: HttpRequest, resp: HttpResponse) {
        val build = req.param("build").toLong()
        val path = req.param("path")
        resp.status = 200
        val e = process.getProcess(JobProcess(buildNumber = build, path = path))
        if (e.status != JobStatusType.PROCESS && e.status != JobStatusType.PREPARE)
            return
        println("e.status=${e.status}")
        val appendable = resp.output.utf8Appendable()
        try {
            while (true) {
                try {
                    LOG.info("wait tail message from #${e.id}")
                    val out = e.topicOut.wait()
                    LOG.info("getted tail message! ${out.message}")

                    when (out) {
                        is Out.Std -> appendable.append("STDOUT:")
                        is Out.Err -> appendable.append("STDERR:")
                    }
                    appendable.append(out.message).append("\n")
                    resp.output.flush()
                } catch (e: ClosedException) {
                    break
                }
            }
        } catch (e: IOException) {
            //NOP
        }
    }

    private suspend fun events(req: HttpRequest, resp: HttpResponse) {
        resp.status = 200
        val appendable = resp.output.utf8Appendable()
        try {
            while (true) {
                try {
                    val out = eventTopic.wait()
                    val vv = ByteArrayOutputStream()

                    JsonRpc.toJSON(out, StructFactory.Class.Struct(out.factory, false))
                            .write(vv.utf8Appendable().asAsync())
                    vv.utf8Appendable().append("\n")
                    resp.output.write(vv.toByteArray())
                    //appendable.append("\n")
                    resp.output.flush()
                } catch (e: ClosedException) {
                    break
                }
            }
        } catch (e: IOException) {
            //NOP
        }
    }

    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        try {
            if (req.contextUriWithoutParams == "tail") {
                tail(req, resp)
                return
            }

            if (req.contextUriWithoutParams == "events") {
                events(req, resp)
                return
            }

            resp.status = 200
            val service = req.contextUri
            val request = req.input.utf8Reader().parseJSON().obj
            val result = when (service) {
                "process" -> {
                    JsonRpc.callAsync(DTO_LIST, ProcessService, process, request)
                }
                "tasks" -> {
                    JsonRpc.callAsync(DTO_LIST, TaskManagerService, taskManager, request)
                }
                "nodes" -> {
                    JsonRpc.callAsync(DTO_LIST, NodesService, nodesService, request)
                }
                else -> throw UnknownException("Can't find service \"$service\"")
            }

            result.write(resp.output.utf8Appendable())
            resp.output.flush()
        } catch (e: Throwable) {
            if (e is UnknownException) {
                LOG.info("Exception: ${e.msg}")
            } else {
                LOG.info("Exception: $e")
            }
            if (e !is IOException) {
                LOG.warn("Exception ${e}\n${e.stackTrace.map { "\tat $it" }.joinToString("\n")}")
            }
        }
    }

}
