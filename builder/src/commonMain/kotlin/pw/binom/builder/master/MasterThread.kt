package pw.binom.builder.master

import pw.binom.ByteBufferPool
import pw.binom.flux.RootRouter
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.strong.Strong
import pw.binom.thread.Thread

class MasterThread(strong: Strong, val bind: List<Pair<String, Int>>) : Thread() {
    val rootRouter by strong.service<RootRouter>()

    init {
        require(bind.isNotEmpty())
    }

    val manager = SocketNIOManager()

    override fun run() {
        val server = HttpServer(manager, rootRouter,
                poolSize = 30,
                inputBufferSize = 1024 * 1024 * 3,
                outputBufferSize = 1024 * 1024 * 3
        )
        bind.forEach {
            server.bindHTTP(host = it.first, port = it.second)
        }

        while (!isInterrupted) {
            manager.update(1000)
        }
    }
}