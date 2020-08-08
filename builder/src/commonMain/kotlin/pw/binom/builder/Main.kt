package pw.binom.builder

//import pw.binom.builder.server.Server
import pw.binom.builder.cli.CmdRunner
import pw.binom.builder.master.MasterThread
import pw.binom.builder.master.masterConfig
import pw.binom.process.Signal
import pw.binom.strong.Strong

fun main(args: Array<String>) {
    execute(args, CmdRunner())
}

/*
abstract class NetTask : Cmd() {
    protected val manager = SocketNIOManager()
    private val httpClient = AsyncHttpClient(manager)
    protected abstract val url: URL
    protected val client by lazy { Client(url, httpClient) }
}
*/
class RunServer : Cmd() {
    override val description: String?
        get() = "Starts Master Node"

    private val projectDir by param("project-dir", "Project for search jobs")
            .require()
            .path()
            .createDir()

    private val bind by paramList("bind", "Bind interface")
            .require()
            .hostPort()

    private val rootUri by param("uri", "URI path to Web")
            .default { "/" }
            .require()

    private val telegramToken by param("telegram-token", "Token for Telegram Bot")

    override fun execute(): Result = action {

        val strong = Strong.create(masterConfig(
                bind = bind,
                tasksRoot = projectDir,
                telegramToken = telegramToken
        ))

        val masterThread by strong.service<MasterThread>()

        masterThread.start()
        Signal.addShutdownHook {
            println("Shutdown!")
            masterThread.interrupt()
            masterThread.join()
            println("OK!")
        }
        masterThread.join()
//        Server(
//                jobsPath = projectDir,
//                bind = bind.map { it.host to (it.port ?: it.defaultPort!!) },
//                rootUri = rootUri
//        ).start()
    }
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