package pw.binom.builder.master

import pw.binom.UUID
import pw.binom.builder.common.Action
import pw.binom.io.http.websocket.WebSocketClosedException
import pw.binom.io.httpServer.websocket.WebSocketHandler
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.printStacktrace
import pw.binom.strong.Strong

class SlaveHandler(val strong: Strong) : WebSocketHandler() {
    private val slaveService by strong.service<SlaveService>()
    private val actionExecutor by strong.service<ActionExecutor>()

    override suspend fun connected(request: ConnectRequest) {
        val slaveId = try {
            request.headers["X-Slave-Id"]?.singleOrNull()?.let { UUID.fromString(it) }
        } catch (e: Throwable) {
            request.reject()
            throw e
        }
        val slaveName = request.headers["X-Slave-Name"]?.singleOrNull()
        val tags = request.headers["X-Slave-Tags"]
                ?.singleOrNull()
                ?.splitToSequence(',')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.toSet()
                ?: emptySet()
        if (slaveId == null || slaveName == null) {
            request.reject()
            return
        }

        if (slaveService.nodeExist(slaveId)) {
            request.reject()
        }

        val connection = request.accept()
        val slave = slaveService.reg(
                name = slaveName,
                tags = tags,
                id = slaveId,
                connection = connection
        )
        connection.incomeMessageListener = {
            try {
                val messageText = it.read().utf8Reader().use {
                    it.readText()
                }
                val action = Action.toAction(messageText)
                actionExecutor.submit(slave, action)
//                action.executeMaster(slave, strong)
            } catch (e: WebSocketClosedException) {
                slaveService.delete(slaveId)
            } catch (e: Throwable) {
                e.printStacktrace()
                slaveService.delete(slaveId)
                connection.close()
            }
        }
    }

}