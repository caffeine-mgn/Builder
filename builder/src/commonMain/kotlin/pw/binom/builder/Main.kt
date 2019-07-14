package pw.binom.builder

import pw.binom.Environment
import pw.binom.URL
import pw.binom.async
import pw.binom.builder.common.ExecuteJob
import pw.binom.builder.common.NodeStatus
import pw.binom.builder.node.Node
import pw.binom.builder.server.Server
import pw.binom.getEnv
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.json.JsonDomReader
import pw.binom.json.JsonReader
import pw.binom.json.array

fun main(args: Array<String>) {
    execute(args, CmdRunner())
}

class RunServer : Function() {
    override val description: String?
        get() = "Starts Build Server"
    private val projectDir by param("project-dir", "Project for search jobs")
            .require()
            .file()
            .dirExist()

    private val bind by paramList("bind", "Bind interface")
            .require()
            .url()

    override fun execute(): Result = action {
        Server(
                jobsPath = projectDir,
                bind = bind.map { it.host to (it.port ?: it.defaultPort!!) }
        ).start()
    }
}

class RunNode : Function() {
    override val description: String?
        get() = "Starts Build Node"
    val envs by paramList("env").convert {
        it.asSequence().map {
            val item = it.split('=', limit = 2)
            item[0] to item[1]
        }.toMap()
    }
    private val bashPath by param("bash-path", "Path to Bash. For example, on linux using \"/bin/bash\". On Windows use cygwin")
            .require()
            .file()
            .fileExist()
    private val buildPath by param("build-path")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .file()
            .dirExist()
    private val serverUrl by param("server")
            .require()
            .url()

    private val id by param("id")
            .require()
            .notBlank()

    private val dataCenter by param("dc")
            .require()
            .notBlank()

    override fun execute(): Result = action {
        Node(
                bashPath = bashPath,
                buildPath = buildPath,
                url = serverUrl.toString(),
                id = id,
                dataCenter = dataCenter,
                envs = envs
        ).start()
    }
}

class CmdRunner : Function() {
    override val description: String?
        get() = "Starter"

    override fun execute(): Result =
            dir(
                    "server" to RunServer(),
                    "node" to RunNode(),
                    "start" to StartJob(),
                    "nodes" to NodesCmd()
            )

}

class StartJob : Function() {
    private val jobId by param("job").require()
    private val serverUrl by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override fun execute(): Result =
            action {
                val manager = ConnectionManager()
                val client = AsyncHttpClient(manager)
                var done = false
                async {
                    client.request("POST", URL("${serverUrl.toString().removeSuffix("/")}/tasks/$jobId/execute")).use {
                        when (val e = it.responseCode()) {
                            200, 204 -> {
                                val job = ExecuteJob.read(it.inputStream.utf8Reader())
                                println("Job ${job.path}:${job.buildNumber} started")
                            }
                            else -> println("Can't start job $jobId. Server returned status $e")
                        }
                    }
                    done = true
                }

                while (!done) {
                    manager.update(1000)
                }
                client.close()
                manager.close()
            }

    override val description: String?
        get() = "Starts Job"
}

class NodesCmd : Function() {
    val drawWithHeader by flag("h", "Enable header of result table")
    private val serverUrl by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override fun execute(): Result =
            action {
                val manager = ConnectionManager()
                val client = AsyncHttpClient(manager)
                var done = false
                async {
                    client.request("GET", URL("${serverUrl.toString().removeSuffix("/")}/nodes")).use {
                        when (val e = it.responseCode()) {
                            200 -> {
                                val r = JsonDomReader()
                                JsonReader(it.inputStream.utf8Reader()).accept(r)
                                val table = Table()
                                table.addHeader("ID")
                                table.addHeader("Data Center")
                                table.addHeader("Platform")
                                table.addHeader("Job Name")
                                table.addHeader("Job Build Number")
                                r.node.array.map {
                                    NodeStatus.read(it)
                                }.forEach {
                                    if (it.job == null)
                                        table.row(it.node.id, it.node.dataCenter, it.node.platform.name)
                                    else
                                        table.row(it.node.id, it.node.dataCenter, it.node.platform.name, it.job.path, it.job.buildNumber.toString())
                                }
                                table.print(drawWithHeader, ConsoleAppendable)
                            }
                            else -> println("Can't get nodes. Server returned status $e")
                        }
                    }
                    done = true
                }

                while (!done) {
                    manager.update(1000)
                }
                client.close()
                manager.close()
            }

    override val description: String?
        get() = "Print all nodes"
}

val SERVER_ADDR = "CI_SERVER"