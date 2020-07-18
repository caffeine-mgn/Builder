package pw.binom.builder.cli

import pw.binom.Environment
import pw.binom.builder.*
import pw.binom.getEnv
import kotlin.Result
/*
class CancelCmd : NetTask() {

    private val jobId by param("job").require()
    private val build by param("build").require().long()
    override val url by param("server")
            .default { Environment.getEnv(SERVER_ADDR) }
            .require()
            .url()

    override val description: String?
        get() = "Cancel the job"

    override fun execute(): pw.binom.builder.Result =
            action {
                manager.sync { client.processService.cancel(JobProcess(buildNumber = build, path = jobId)) }
            }

}
*/