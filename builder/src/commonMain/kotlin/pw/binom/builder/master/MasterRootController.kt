package pw.binom.builder.master

import pw.binom.flux.RootRouter
import pw.binom.strong.Strong

class MasterRootController(strong: Strong) : Strong.InitializingBean {
    private val slave by strong.service<SlaveHandler>()
    private val events by strong.service<EventBusHandler>()

    /*
        override suspend fun request(req: HttpRequest, resp: HttpResponse) {
            if (req.contextUri == "/slave") {
                slave.request(req, resp)
                return
            }

            if (req.contextUri == "/events") {
                events.request(req, resp)
                return
            }

            println("Request [${req.method}] [${req.uri}]")
            resp.status = 404
            resp.complete()
            /*
            if (req.method == "GET" && req.contextUri == "/job") {
                val slaveId = req.headers["X-Slave-Id"]?.singleOrNull()?.let { UUID.fromString(it) }
                val node = master.slaveService.nodes[slaveId]
                if (node == null) {
                    resp.status = 404
                    resp.complete()
                } else {
                    val complete = resp.complete()
                    val appender = complete.utf8Appendable()
                    while (!Thread.currentThread.isInterrupted) {
                        try {
                            val event = node.topic.wait()
                            appender.append(event.toJson()).append("\n")
                            complete.flush()
                        } catch (e: Throwable) {
                            e.printStacktrace()
                            break
                        }
                    }
                }
            }
            if (req.method == "POST" && req.contextUri == "/rpc") {
                val action = Action.toAction(req.input.utf8Reader().readText())
                val responseJson = action.executeMaster(master)?.toJson()
                if (responseJson == null) {
                    resp.status = 204
                    resp.complete()
                } else {
                    resp.status = 200
                    resp.complete().also {
                        it.utf8Appendable().append(responseJson)
                        it.flush()
                    }
                }
                return
            }
            if (req.method == "GET" && req.contextUri == "/eventbus") {
                val complete = resp.complete()
                val appender = complete.utf8Appendable()
                try {
                    while (true) {
                        val event = master.globalTopic.wait()
                        appender.append(event.toJson()).append("\n")
                        complete.flush()
                    }
                } catch (e: Throwable) {
                    //NOP
                }
            }
            TODO("Not yet implemented. METHOD: [${req.method}], URI: [${req.uri}]")
            */
        }
    */
    private val rootRouter by strong.service<RootRouter>()

    override fun init() {
        rootRouter.route("/slave").forward(slave)
        rootRouter.route("/events").forward(events)
    }

}