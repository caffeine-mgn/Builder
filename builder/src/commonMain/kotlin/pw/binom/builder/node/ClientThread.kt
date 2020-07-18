package pw.binom.builder.node

import pw.binom.URL
import pw.binom.async
import pw.binom.builder.common.Action
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.readText
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.thread.Thread
import pw.binom.uuid
import kotlin.random.Random

class ClientThread(serverUrl: URL, val name: String) : Thread(), Client {
    val serverUrl = serverUrl.newURI("${serverUrl.uri.removeSuffix("/")}/rpc")
    private val clientId = Random.uuid()

    override fun run() {

        val manager = SocketNIOManager()
        val client = AsyncHttpClient(manager)
        try {
            async {
                while (!isInterrupted) {
                    var action: Action? = Action.NodePing(clientId.toString())
                    while (action != null) {
                        val acc = action
                        action = null
                        try {
                            action = client.request("POST", serverUrl)
                                    .upload()
                                    .also {
                                        it.utf8Appendable().append(acc.toJson())
                                        it.flush()
                                    }
                                    .response().use { response ->
                                        when (response.responseCode) {
                                            200 -> Action.toAction(response.utf8Reader().readText())
                                                    .executeSlave(this)
                                            204 -> null
                                            else -> TODO()
                                        }
                                    }
                        } catch (e: Throwable) {

                        }
                    }
                    Thread.sleep(5000)
                }
            }
            while (!isInterrupted) {
                manager.update(1000)
            }
        } finally {
            client.close()
            manager.close()
        }
    }
}