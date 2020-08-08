package pw.binom.builder.web.services

import org.w3c.dom.WebSocket
import pw.binom.builder.Event
import pw.binom.builder.web.serverUrl
import pw.binom.io.Closeable

object EventService {
    private val listeners = ArrayList<(Event) -> Unit>()

    fun addListener(listener: (Event) -> Unit): Closeable {
        listeners += listener
        return Closeable {
            listeners -= listener
        }
    }

    private fun onMessage(msg: String) {
        console.info("Event:", JSON.parse(msg))
        val event = Event.toEvent(msg)
        listeners.toTypedArray().forEach {
            it(event)
        }
    }

    init {
        var wsUrl = when {
            serverUrl.startsWith("https://") -> "wss://${serverUrl.removePrefix("https://")}"
            serverUrl.startsWith("http://") -> "ws://${serverUrl.removePrefix("http://")}"
            else -> TODO()
        }
        wsUrl = "$wsUrl/events"
        var socket = WebSocket(wsUrl)
        socket.onmessage = {
            onMessage(it.data as String)
        }
    }
}