package pw.binom.builder.master.controllers

import pw.binom.UUID
import pw.binom.builder.common.NodeDto
import pw.binom.builder.master.services.SlaveService
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.flux.RootRouter
import pw.binom.io.http.websocket.WebSocketClosedException
import pw.binom.io.httpServer.websocket.WebSocketHandler
import pw.binom.io.readText
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.strong.Strong

class SlaveController(val strong: Strong) : WebSocketHandler(), Strong.InitializingBean {
    private val slaveService by strong.service<SlaveService>()
//    private val actionExecutor by strong.service<ActionExecutor>()
    val taskStorage by strong.service<TaskStorage>()
    private val rootRouter by strong.service<RootRouter>()

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
        println("Node conntected!")
        try {
            while (true) {
                val msg = connection.read()
                val dto = msg.utf8Reader().use {
                    NodeDto.toDto(it.readText())
                }
                val u = when (dto) {
                    is NodeDto.ChangeState -> {
                        println("Change state to ${dto.status}")
                        slave.status = if (dto.status.terminateState)
                            null
                        else
                            SlaveService.SlaveStatus(jobPath = dto.path, dto.buildNumber)
                        (taskStorage.getEntity(dto.path) as TaskStorage.Job).getBuild(dto.buildNumber)!!.status = dto.status
                    }
                    is NodeDto.Log -> {
                        val status = slave.status!!
                        val build = (taskStorage.getEntity(status.jobPath) as TaskStorage.Job).getBuild(status.buildNumber)!!
                        if (dto.err)
                            build.addStderr(dto.text)
                        else
                            build.addStdout(dto.text)
                    }
                }
            }
        } catch (e: WebSocketClosedException) {
            println("Node disconnected!")
            slaveService.delete(slaveId)
        } catch (e: Throwable) {
            e.printStackTrace()
            slaveService.delete(slaveId)
            connection.close()
        }

//        connection.incomeMessageListener = {
//            try {
//                val messageText = it.read().utf8Reader().use {
//                    it.readText()
//                }
//                val action = Action.toAction(messageText)
//                actionExecutor.submit(slave, action)
////                action.executeMaster(slave, strong)
//            } catch (e: WebSocketClosedException) {
//                slaveService.delete(slaveId)
//            } catch (e: Throwable) {
//                e.printStacktrace()
//                slaveService.delete(slaveId)
//                connection.close()
//            }
//        }
    }

    override fun init() {
        rootRouter.route("/slave").forward(this)
    }

}