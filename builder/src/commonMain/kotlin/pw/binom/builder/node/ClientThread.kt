package pw.binom.builder.node

import pw.binom.URL
import pw.binom.async
import pw.binom.builder.common.Action
import pw.binom.builder.master.SlaveService
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.io.IOException
import pw.binom.io.file.File
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.readText
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.strong.Strong
import pw.binom.thread.Lock
import pw.binom.thread.Thread
import pw.binom.thread.synchronize
import pw.binom.uuid
import kotlin.random.Random

class ClientThread(
        val strong: Strong,
        val serverUrl: URL,
        val name: String,
        val tags: Set<String>,
        val bashPath: File
) : Thread() {
    val slaveId = Random.uuid()

    var state: SlaveService.SlaveStatus? = null
        set(value) {
            lock.synchronize {
                if (field != value) {
                    sendStatusUpdate(value)
                    if (value == null) {
                        println("Clean Current Job")
                        currentJob = null
                    }
                }
                field = value
            }
        }

    private fun sendStatusUpdate(status: SlaveService.SlaveStatus?) {
        publishThread.publish(Action.SlaveChangeState(status))
//        async {
//            try {
//                println("Send status update to ${status}")
//                currentConnection?.write(MessageType.TEXT)?.utf8Appendable()?.use {
//                    it.append(Action.SlaveChangeState(status).toJson())
//                }
//            } catch (e: Throwable) {
//                e.printStacktrace()
//            }
//        }
    }

    internal var currentConnection: WebSocketConnection? = null

    private var currentJob: JobRunner? = null
    private val buildManager by strong.service<BuildManager>()
    private val lock = Lock()
    private val publishThread = PublishThread(this)
    fun send(action: Action) {
        publishThread.publish(action)
//        val connection = currentConnection
//        if (connection != null) {
//            async {
//                try {
//                    println("Sending $action")
//                    connection.write(MessageType.TEXT).utf8Appendable().use {
//                        it.append(action.toJson())
//                    }
//                    println("Send $action")
//                } catch (e: Throwable) {
//                    e.printStacktrace()
//                }
//            }
//        } else {
//            println("Can't send $action")
//        }
    }

    fun startBuild(config: TaskStorage.JobConfig, path: String, buildNumber: Int): Boolean {
        lock.synchronize {
            if (currentJob != null) {
                println("Current Job exist!")
                return false
            }
            val dir = buildManager.prepareBuildDir()
            state = SlaveService.SlaveStatus(
                    jobPath = path,
                    buildNumber = buildNumber
            )
            val runner = JobRunner(
                    config = config,
                    buildNumber = buildNumber,
                    path = path,
                    bashPath = bashPath,
                    dir = dir,
                    strong = strong
            )
            runner.eventStatusUpdate.on {
                send(Action.TaskStatusChange(
                        path = path,
                        buildNumber = buildNumber,
                        status = runner.status
                ))

                if (runner.status.terminateState) {
                    println("Runner ok. clear current")
                    currentJob = null
                    state = null
                }
            }
            runner.start()
            currentJob = runner
            return true
        }
    }

    override fun run() {
        publishThread.start()
        val manager = SocketNIOManager()
        val client = AsyncHttpClient(manager)
        val serverUrl1 = serverUrl.newURI("${serverUrl.uri.removeSuffix("/")}/slave")
        try {
            println("WS Thread: ${Thread.currentThread.id}")
            async {
                while (!isInterrupted) {
                    try {
                        val connection = client.request("GET", serverUrl1)
                                .addHeader("X-Slave-Id", slaveId.toString())
                                .addHeader("X-Slave-Name", name)
                                .addHeader("X-Slave-Tags", tags.joinToString(", "))
                                .websocket()
                        currentConnection = connection
                        state = null
                        while (!isInterrupted) {
                            val json = connection.read().utf8Reader().use {
                                it.readText()
                            }
                            val action = Action.toAction(json)
                            println("Getting action: ${action::class.simpleName}")
                            val response = action.executeSlave(strong)
                            if (response != null) {
                                send(response)
//                                connection.write(MessageType.TEXT).utf8Appendable().use {
//                                    it.append(response.toJson())
//                                }
                            }
                        }
                    } catch (e: IOException) {
                        currentConnection = null
                        sleep(10_000)
                    }
                }
            }
            while (!isInterrupted) {
                manager.update()
            }
        } finally {
            client.close()
            manager.close()
        }
    }

    fun cancelBuild() {
        currentJob?.close()
        currentJob = null
        state = null
    }
}