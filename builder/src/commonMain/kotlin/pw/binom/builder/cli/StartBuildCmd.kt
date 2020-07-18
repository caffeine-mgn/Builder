package pw.binom.builder.cli

import pw.binom.Environment
import pw.binom.builder.*
import pw.binom.getEnv
/*
class StartJob : NetTask() {
    private val jobId by param("job").require()
    override val url by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override fun execute(): Result =
            action {
//                val exe = manager.sync { client.processService.execute(jobId) }
//                println("Start job ${exe.path}:${exe.buildNumber}")
                TODO("Start task not ready")
            }

    override val description: String?
        get() = "Starts Job"
}
*/