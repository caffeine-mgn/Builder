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

class EventBusHandler(val strong: Strong) : WebSocketHandler(), Strong.InitializingBean {
    private val eventSystem by strong.service<EventSystem>()
    private val rootRouter by strong.service<RootRouter>()

    private var clients = HashSet<WebSocketConnection>()
    private val skipBuffer = ByteBuffer.alloc(DEFAULT_BUFFER_SIZE)

    override suspend fun connected(request: ConnectRequest) {
        val connection = run {
            val connection = request.accept()
            clients.add(connection)
            connection
        }
        while (true) {
            try {
                connection.read().use {
                    it.skipAll(skipBuffer)
                }
            } catch (e: WebSocketClosedException) {
                clients.remove(connection)
            }
        }
    }

    override fun init() {

        rootRouter.route("/events").forward(this)

        eventSystem.listen(Event::class) { event ->
            println("Event got! $event. Listener count: ${clients.size}")
            async {
                try {
                    val eventJson = event.toJson()
                    println("Try send $eventJson")
                    clients.forEach {
                        val result = runCatching {
                            it.write(MessageType.TEXT).utf8Appendable().use {
                                it.append(eventJson)
                            }
                        }
                        if (result.isFailure) {
                            result.exceptionOrNull()!!.printStackTrace()
                        }
                    }
                } catch (e:Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}