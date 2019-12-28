package pw.binom.builder

import pw.binom.Environment
import pw.binom.URL
import pw.binom.builder.client.Client
import pw.binom.builder.common.JobEntity
import pw.binom.builder.node.Node
import pw.binom.builder.remote.JobProcess
import pw.binom.builder.server.Server
import pw.binom.getEnv
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.ConnectionManager

fun main(args: Array<String>) {
    execute(args, CmdRunner())
}

abstract class NetTask : Function() {
    protected val manager = ConnectionManager()
    private val httpClient = AsyncHttpClient(manager)
    protected abstract val url: URL
    protected val client by lazy { Client(url, httpClient) }
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

    private val rootUri by param("uri", "URI path to Web")
            .default { "/" }
            .require()

    override fun execute(): Result = action {
        Server(
                jobsPath = projectDir,
                bind = bind.map { it.host to (it.port ?: it.defaultPort!!) },
                rootUri = rootUri
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
    val tags by paramList("tag")

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
//                    "nodes" to NodesCmd(),
//                    "tail" to TailCmd(),
                    "cancel" to CancelCmd()
//                    "executes" to ExecutesCmd(),
//                    "tasks" to TasksJob()
            )
}
/*
class TasksJob : NetTask() {
    override val description: String?
        get() = "Print task by path"
    private val path by param("path").default { "/" }.require()

    override val url by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override fun execute(): Result =
            action {
                val tasks = client.tasks(path)
                val table = Table()
                table.addHeader("Name")
                table.addHeader("Type")
                tasks.forEach {
                    val type = when (it) {
                        is JobEntity.Job -> "JOB"
                        is JobEntity.Folder -> "FOLDER"
                    }
                    table.row(it.name, type)
                }
                table.print(true, ConsoleAppendable)
            }
}
*/
/*
class ExecutesCmd : Function() {
    override val description: String?
        get() = "Print task by path"

    private val serverUrl by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override fun execute(): Result =
            action {
                val client = Client(serverUrl)
                val tasks = client.executions()
                val table = Table()
                table.addHeader("Path")
                table.addHeader("Build Number")
                table.addHeader("Node")
                tasks.forEach {
                    table.row(it.job.path, it.job.buildNumber.toString(), it.node?.id ?: "")
                }
                table.print(true, ConsoleAppendable)
            }
}
*/
class StartJob : NetTask() {
    private val jobId by param("job").require()
    override val url by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override fun execute(): Result =
            action {
                val exe = manager.sync { client.processService.execute(jobId) }
                println("Start job ${exe.path}:${exe.buildNumber}")
            }

    override val description: String?
        get() = "Starts Job"
}
/*
class TailCmd : Function() {

    private val jobId by param("job").require()
    private val build by param("build").require().long()
    private val serverUrl by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override val description: String?
        get() = "Tail build process output"

    override fun execute(): Result =
            action {
                val client = Client(serverUrl)
                client.tail(job = jobId, buildNumber = build, appendable = ConsoleAppendable)
            }

}
*/
class CancelCmd : NetTask() {

    private val jobId by param("job").require()
    private val build by param("build").require().long()
    override val url by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override val description: String?
        get() = "Cancel the job"

    override fun execute(): Result =
            action {
                manager.sync { client.processService.cancel(JobProcess(buildNumber = build, path = jobId)) }
            }

}

/*
class NodesCmd : Function() {
    val drawWithHeader by flag("h", "Enable header of result table")
    private val serverUrl by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override fun execute(): Result =
            action {
                val client = Client(serverUrl)
                val table = Table()
                table.addHeader("ID")
                table.addHeader("Data Center")
                table.addHeader("Platform")
                table.addHeader("Job Name")
                table.addHeader("Job Build Number")
                client.status().forEach {
                    if (it.job == null)
                        table.row(it.node.id, it.node.dataCenter, it.node.platform.name)
                    else
                        table.row(it.node.id, it.node.dataCenter, it.node.platform.name, it.job.path, it.job.buildNumber.toString())
                }
                table.print(drawWithHeader, ConsoleAppendable)
            }

    override val description: String?
        get() = "Print all nodes"
}
*/
val SERVER_ADDR = "CI_SERVER"