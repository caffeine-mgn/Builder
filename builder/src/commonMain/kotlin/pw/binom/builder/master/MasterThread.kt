package pw.binom.builder.master

import pw.binom.builder.common.Action
import pw.binom.io.httpServer.Handler
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.HttpServer
import pw.binom.io.readText
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.thread.Thread

class MasterThread(val bind: List<Pair<String, Int>>) : Thread(), Master {
    init {
        require(bind.isNotEmpty())
    }

    override fun run() {
        val handler = MasterHanler(this)
        val manager = SocketNIOManager()

        val server = HttpServer(manager, handler)
        bind.forEach {
            server.bindHTTP(host = it.first, port = it.second)
        }

        while (!isInterrupted) {
            manager.update(1000)
        }
    }
}

class MasterHanler(val master: Master) : Handler {
    override suspend fun request(req: HttpRequest, resp: HttpResponse) {
        println("Request [${req.method}] [${req.uri}]")
        if (req.method == "POST" && req.contextUri == "/rpc") {
            val inputJson = req.input.utf8Reader().readText()
            println("inputJson:\n$inputJson")
            val action = Action.toAction(inputJson)
            val responseJson = action.executeMaster(master)?.toJson()
            if (responseJson == null) {
                resp.status = 204
                resp.complete()
            } else {
                resp.status = 200
                println("responseJson:\n$responseJson")
                resp.complete().also {
                    it.utf8Appendable().append(responseJson)
                    it.flush()
                }
            }
            return
        }
        TODO("Not yet implemented. METHOD: [${req.method}], URI: [${req.uri}]")
    }

}