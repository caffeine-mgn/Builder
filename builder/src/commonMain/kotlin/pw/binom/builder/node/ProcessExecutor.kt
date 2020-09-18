package pw.binom.builder.node

import pw.binom.Environment
import pw.binom.atomic.AtomicBoolean
import pw.binom.builder.OutType
import pw.binom.builder.master.taskStorage.TaskStorage
import pw.binom.doFreeze
import pw.binom.getEnvs
import pw.binom.io.file.File
import pw.binom.io.file.write
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.process.Process
import pw.binom.process.execute

class ProcessExecutor(
        val cmd: String,
        val args: List<String>,
        val env: Map<String, String>,
        val workDirectory: String,
        val output: Output
) {
    companion object {
        fun runBash(buildNumber: Int,
                    dir: File,
                    bashPath: File,
                    output: Output,
                    config: TaskStorage.JobConfig): ProcessExecutor {
            println("Dir: $dir, bashPath: $bashPath")
            val env = HashMap(Environment.getEnvs())
            env.putAll(config.env)
            env["BUILD_NUMBER"] = buildNumber.toString()
            env.putAll(env)
            val scriptFile = File(dir, "script.sh")
            scriptFile.write().use {
                it.utf8Appendable().append(config.cmd)
            }

            return ProcessExecutor(
                    cmd = bashPath.path,
                    workDirectory = dir.path,
                    args = listOf(scriptFile.path),
                    env = env,
                    output = output
            )
        }
    }

    interface Output {
        fun std(text: String)
        fun err(text: String)
        fun status(status: TaskStorage.JobStatusType)
    }

    private val stdoutDone = AtomicBoolean(false)
    private val stderrDone = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)

    private fun log(process: Process, type: OutType, text: String?): Boolean {
        if (text == null) {
            when (type) {
                OutType.STDOUT -> stdoutDone.value = true
                OutType.STDERR -> stderrDone.value = true
            }

            println("End of stream! type: $type, out: [${stdoutDone.value}], err: [${stderrDone.value}]")

            if (stdoutDone.value && stderrDone.value) {
                if (cancelled.value) {
                    output.status(TaskStorage.JobStatusType.CANCELED)
                    return false
                }
                process.join()
                if (process.exitStatus == 0)
                    output.status(TaskStorage.JobStatusType.FINISHED_OK)
                else
                    output.status(TaskStorage.JobStatusType.FINISHED_ERROR)
            }
            return false
        }

        when (type) {
            OutType.STDOUT -> output.std(text)
            OutType.STDERR -> output.err(text)
        }

        return cancelled.value
    }

    init {
        output.status(TaskStorage.JobStatusType.PREPARE)
    }

    val process = Process.execute(
            path = cmd,
            workDir = workDirectory,
            args = args,
            env = env)

    init{
        output.status(TaskStorage.JobStatusType.PROCESS)
    }

    val stdReader = ThreadReader2(
            process.stdout
    ) {
        log(process, OutType.STDOUT, it)
    }

    val errReader = ThreadReader2(
            process.stderr
    ) {
        log(process, OutType.STDERR, it)
    }

    init {
        doFreeze()
    }
}