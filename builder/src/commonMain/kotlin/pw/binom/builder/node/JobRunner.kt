package pw.binom.builder.node
/*
import pw.binom.Environment
import pw.binom.builder.OutType
import pw.binom.builder.client.Client
import pw.binom.builder.remote.BuildDescription
import pw.binom.builder.remote.asShort
import pw.binom.builder.remote.toProcess
import pw.binom.getEnvs
import pw.binom.io.Closeable
import pw.binom.io.file.File
import pw.binom.io.file.write
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.popOrNull
import pw.binom.process.Process
import pw.binom.process.execute
import pw.binom.stackTrace
import pw.binom.thread.FreezedStack
import pw.binom.thread.Thread
import kotlin.collections.set

class JobRunner(url: String,
                val dir: File,
                val bashPath: File,
                val job: BuildDescription,
                val client: Client,
                val envs: Map<String, String>) : Closeable {
    val url = url.removeSuffix("/")
    val out = FreezedStack<Out>().asFiFoQueue()

    //    private var events: JobActionListener? = null
    private var cancelled = false
    private val log = Logger.getLog("Runner ${job.toProcess().asShort}")

//    private fun actionProcess() {
//        val events = events!!.actions
//        while (!events.isEmpty) {
//            val event = events.pop()
//            when (event) {
//                is ActionCancel -> cancelled = true
//            }
//        }
//    }

    suspend fun build() {
        log.info("Start Building...")
//        events = JobActionListener(
//                url = URL(url),
//                job = job.toProcess()
//        )
        try {
            val scriptFile = File(dir, "script.sh")
            scriptFile.write().use {
                it.utf8Appendable().append(job.cmd)
            }
            val env = HashMap(Environment.getEnvs())
            env.putAll(this.envs)
            env["BUILD_NUMBER"] = job.buildNumber.toString()
            env.putAll(job.env.associate { it.name to it.value })
            client.processService.start(job.toProcess())
            val process = Process.execute(
                    path = bashPath.path,
                    workDir = dir.path,
                    args = listOf(scriptFile.path),
                    env = env)
            val stdout = ThreadReader(process.stdout, OutType.STDOUT, out)
            val stderr = ThreadReader(process.stderr, OutType.STDERR, out)

            stdout.start()
            stderr.start()

            var stdoutDone = false
            var stderrDone = false

            while (true) {
                try {
//                    actionProcess()
                    if (cancelled) {
                        log.info("Task Cancelled")
                        stdout.interrupt()
                        stderr.interrupt()
                        stderrDone = true
                        stdoutDone = true
                        process.close()
                        sendCancel()
                        break
                    }
                    if (stderrDone && stdoutDone) {
                        break
                    }
                    val o = out.popOrNull()
                    if (o == null) {
                        Thread.sleep(1000)
                        continue
                    }
                    if (o.value == null) {
                        when (o.type) {
                            OutType.STDOUT -> stdoutDone = true
                            OutType.STDERR -> stderrDone = true
                        }
                    } else {
                        if (!sendOut(o))
                            cancelled = true
                    }
                } catch (e: Throwable) {
                    println("JobRunner::build::loop error: $e")
                    e.stackTrace
                }
            }
            stdout.interrupt()
            stderr.interrupt()
            try {
                sendFinish(process.exitStatus == 0)
            } catch (e: Process.ProcessStillActive) {
                sendFinish(false)
            }
        } catch (e: Throwable) {
            println("Error build ${job.path}:${job.buildNumber}   $e")
            sendFinish(false)
        }
//        events?.close()
    }

    private suspend fun sendCancel() {
        client.processService.cancelled(job.toProcess())
    }

    private suspend fun sendFinish(ok: Boolean) {
        client.processService.finish(job.toProcess(), ok)
    }

    private suspend fun sendOut(out: Out): Boolean {
        if (out.value != null)
            return when (out.type) {
                OutType.STDOUT -> client.processService.stdout("${job.path}:${job.buildNumber}", out.value)
                OutType.STDERR -> client.processService.stderr("${job.path}:${job.buildNumber}", out.value)
            }

        return true
    }

    override fun close() {
        cancelled = true
    }
}

 */