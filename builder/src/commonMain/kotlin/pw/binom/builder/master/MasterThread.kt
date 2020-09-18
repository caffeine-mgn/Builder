package pw.binom.builder.master

import pw.binom.flux.RootRouter
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.process.Signal
import pw.binom.strong.Strong

class MasterThread(strong: Strong, val bind: List<Pair<String, Int>>) {
    val rootRouter by strong.service<RootRouter>()
    val manager by strong.service<SocketNIOManager>()

    init {
        require(bind.isNotEmpty())
    }

    fun run() {
        val server = HttpServer(manager, rootRouter,
                poolSize = 30,
                inputBufferSize = 1024 * 1024 * 3,
                outputBufferSize = 1024 * 1024 * 3
        )
        bind.forEach {
            server.bindHTTP(host = it.first, port = it.second)
        }

        while (!Signal.isInterrupted) {
            manager.update(1000)
        }
    }
}