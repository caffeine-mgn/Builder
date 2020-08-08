package pw.binom.builder.master.controllers

import pw.binom.*
import pw.binom.builder.Event
import pw.binom.flux.RootRouter
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketClosedException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.websocket.WebSocketHandler
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.strong.EventSystem
import pw.binom.strong.Strong
import pw.binom.thread.Lock
import pw.binom.thread.synchronize

class EventBusHandler(val strong: Strong) : WebSocketHandler(), Strong.InitializingBean {
    private val eventSystem by strong.service<EventSystem>()
    private val clientsLock = Lock()
    private val rootRouter by strong.service<RootRouter>()

    private var clients = HashSet<WebSocketConnection>()
    private val skipBuffer = ByteBuffer.alloc(DEFAULT_BUFFER_SIZE)

//    init {
//        async {
//            while (true) {
//                try {
//                    val event = eventTopic.wait()
//                    val eventJson = event.toJson()
//                    clients.forEach {
//                        val r = runCatching {
//                            it.write(MessageType.TEXT).utf8Appendable().use {
//                                it.append(eventJson)
//                            }
//                        }
//                        if (r.isFailure) {
//                            println("Error: ${r.exceptionOrNull()}")
//                        }
//                    }
//                } catch (e: Throwable) {
//                    e.printStacktrace()
//                }
//            }
//        }
//    }

    override suspend fun connected(request: ConnectRequest) {
        val connection = clientsLock.synchronize {
            val connection = request.accept()
            clients.add(connection)
            connection
        }
        connection.incomeMessageListener = {
            try {
                it.read().use {
                    it.skipAll(skipBuffer)
                }
            } catch (e: WebSocketClosedException) {
                clientsLock.synchronize {
                    clients.remove(it)
                }
            }
        }
    }

    override fun init() {

        rootRouter.route("/events").forward(this)

        eventSystem.listen(Event::class) { event ->
            async {
                val eventJson = event.toJson()
                clients.forEach {
                    val result = runCatching {
                        it.write(MessageType.TEXT).utf8Appendable().use {
                            it.append(eventJson)
                        }
                    }
                    if (result.isFailure) {
                        result.exceptionOrNull()!!.printStacktrace()
                    }
                }
            }
        }
    }
}