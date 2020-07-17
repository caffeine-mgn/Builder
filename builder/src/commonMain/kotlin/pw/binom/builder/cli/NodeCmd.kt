package pw.binom.builder.cli

import pw.binom.Environment
import pw.binom.builder.*
import pw.binom.getEnv
import kotlin.Function
import kotlin.Result

class RunNode : pw.binom.builder.Function() {
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

    override fun execute(): pw.binom.builder.Result = action {
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