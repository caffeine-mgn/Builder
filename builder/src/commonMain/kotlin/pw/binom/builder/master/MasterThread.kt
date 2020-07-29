package pw.binom.builder.master

import pw.binom.flux.RootRouter
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.strong.Strong
import pw.binom.thread.Thread

class MasterThread(strong: Strong, val bind: List<Pair<String, Int>>) : Thread() {

    val masterHanler by strong.service(MasterRootController::class)
    val rootRouter by strong.service<RootRouter>()

    init {
        require(bind.isNotEmpty())
    }

    val manager = SocketNIOManager()

    override fun run() {
        val server = HttpServer(manager, rootRouter)
        bind.forEach {
            server.bindHTTP(host = it.first, port = it.second)
        }

        while (!isInterrupted) {
            val r = manager.update()
        }
    }
}