package pw.binom.builder.cli

import pw.binom.Environment
import pw.binom.builder.*
import pw.binom.builder.node.ClientThread
import pw.binom.builder.node.slaveConfig
import pw.binom.getEnv
import pw.binom.process.Signal
import pw.binom.strong.Strong

class RunNode : pw.binom.builder.Cmd() {
    override val description: String?
        get() = "Starts Worker"
    val envs by paramList("env").convert {
        it.asSequence().map {
            val item = it.split('=', limit = 2)
            item[0] to item[1]
        }.toMap()
    }
    val tags by paramList("tag").convert { it.toSet() }

    private val bashPath by param("bash-path", "Path to Bash. For example, on linux using \"/bin/bash\". On Windows use cygwin")
            .require()
            .path()
            .fileExist()

    private val buildPath by param("build-path")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .path()
            .createDir()

    private val count by param("j", "Count of worker")
            .default { "1" }
            .require()
            .toInt()

    private val serverUrl by param("server")
            .require()
            .url()

    private val id by param("id")
            .require()
            .notBlank()

    private val dataCenter by param("dc")
            .require()
            .notBlank()

    override fun execute() = action {
        require(count > 0)
        val threads = (0 until count).map {
            val strong = Strong.create(slaveConfig(
                    serverUrL = serverUrl,
                    name = id,
                    tags = tags,
                    baseDir = buildPath,
                    bashPath = bashPath
            ))
            val clientThread by strong.service<ClientThread>()
            clientThread.start()
            clientThread
        }

        Signal.addShutdownHook {
            threads.forEach {
                it.interrupt()
            }
        }
        threads.forEach {
            it.join()
        }
//        Node(
//                bashPath = bashPath,
//                buildPath = buildPath,
//                url = serverUrl.toString(),
//                id = id,
//                dataCenter = dataCenter,
//                envs = envs
//        ).start()
    }
}