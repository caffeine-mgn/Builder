package pw.binom.builder.node

import pw.binom.URL
import pw.binom.async
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicReference
import pw.binom.builder.common.MasterDto
import pw.binom.builder.common.NodeDto
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.concurrency.Reference
import pw.binom.concurrency.StateHolder
import pw.binom.concurrency.asReference
import pw.binom.io.file.File
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.readText
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import pw.binom.process.Signal
import pw.binom.strong.Strong
import pw.binom.uuid
import kotlin.random.Random

class Client2(
        val strong: Strong
) {
    val config by strong.service<NodeConfig>()
    val manager by strong.service<SocketNIOManager>()
    val slaveId = Random.uuid()
    private val buildManager by strong.service<BuildManager>()
    private var build: ProcessExecutor? = null

    fun start() {
        val client = AsyncHttpClient(manager)
        async {
            try {
                println("Try connect...")
                val serverUrl1 = config.serverUrl.newURI("${config.serverUrl.uri.removeSuffix("/")}/slave")
                val connection = client.request("GET", serverUrl1)
                        .addHeader("X-Slave-Id", slaveId.toString())
                        .addHeader("X-Slave-Name", config.name)
                        .addHeader("X-Slave-Tags", config.tags.joinToString(", "))
                        .websocket()
                println("Connected!")
                while (!Signal.isInterrupted) {
                    println("Reading message from master")
                    val msg = connection.read()
                    println("Read $msg")
                    val dto = MasterDto.toDto(msg.utf8Reader().readText())
                    println("Readed $dto")
                    when (dto) {
                        is MasterDto.StartBuild -> startBuild(connection, dto)
                    }
                }
            } catch (e:Throwable) {
                e.printStackTrace()
                throw e
            }
        }

        while (!Signal.isInterrupted) {
            manager.update(1000)
        }

        client.close()
        manager.close()
    }

    private fun startBuild(con: WebSocketConnection, data: MasterDto.StartBuild) {
        val dir = buildManager.prepareBuildDir()
        val build = ProcessExecutor.runBash(
                buildNumber = data.buildNumber,
                dir = dir,
                bashPath = config.bashPath,
                config = data.config,
                output = OutputImpl(
                        path = data.path,
                        buildNumber = data.buildNumber,
                        con = con,
                        client = asReference()
                )
        )
        this.build = build
    }

    class OutputImpl(val client: Reference<Client2>, val path: String, val buildNumber: Int, val con: WebSocketConnection) : ProcessExecutor.Output {
        override fun std(text: String) {
            con.write(MessageType.TEXT) {
                it.utf8Appendable().use {
                    it.append(NodeDto.Log(
                            err = false,
                            text = text
                    ).toJson())
                }
            }
        }

        override fun err(text: String) {
            con.write(MessageType.TEXT) {
                it.utf8Appendable().use {
                    it.append(NodeDto.Log(
                            err = true,
                            text = text
                    ).toJson())
                }
            }
        }

        override fun status(status: TaskStorage.JobStatusType) {
            con.write(MessageType.TEXT) {
                it.utf8Appendable().use {
                    it.append(
                            NodeDto.ChangeState(
                                    buildNumber = buildNumber,
                                    path = path,
                                    status = status
                            ).toJson()
                    )
                }

                if (status.terminateState) {
                    client.value.build = null
                    client.close()
                }
            }

        }

    }
}