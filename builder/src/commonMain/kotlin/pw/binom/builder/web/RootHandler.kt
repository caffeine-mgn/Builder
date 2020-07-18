package pw.binom.builder.web

/*
import pw.binom.URL
import pw.binom.builder.Topic
import pw.binom.builder.remote.NodesServiceAsync
import pw.binom.builder.remote.TaskManagerServiceAsync
import pw.binom.builder.server.*
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.krpc.Struct
import pw.binom.logger.Logger
import pw.binom.logger.warn

class RootHandler(
        taskManager: TaskManager1,
        process: ProcessServiceImpl,
        taskManagerService: TaskManagerServiceAsync,
        eventTopic: Topic<Struct>,
        nodesService: NodesServiceAsync,
        rootUri:String
) : PathHandler() {
    private val LOG = Logger.getLog("/")
    private val webUiHandler = WebUiHandler(URL(rootUri))
    private val apiHandler = ApiHandler(process, taskManagerService, taskManager, eventTopic, nodesService)

    init {
        filter(startsWith("/"), webUiHandler)
        filter(startsWith("/api/")/* + method("POST")*/, apiHandler)
    }

    override suspend fun exceptionHander(exception: Throwable, req: HttpRequest, resp: HttpResponse) {
        if (exception is APIException) {
            resp.status = 400
            LOG.warn("${req.contextUri}: ${exception.message}")
        }
        super.exceptionHander(exception, req, resp)
    }

    open class APIException(message: String) : RuntimeException(message)
}

fun HttpRequest.param(name: String) = params[name]
        ?: throw RootHandler.APIException("Request Param \"$name\" not exist. $method ${contextUri}")

fun HttpRequest.pageNotFound(): Nothing = throw RootHandler.APIException("Page ${method} ${this.contextUri} not found")

 */