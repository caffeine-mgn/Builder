package pw.binom.builder.node

import pw.binom.Environment
import pw.binom.builder.OutType
import pw.binom.builder.common.Action
import pw.binom.builder.events.EventElement
import pw.binom.builder.master.taskStorage.TaskStorage
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
import pw.binom.strong.Strong
import pw.binom.thread.FreezedStack
import pw.binom.thread.Thread
import kotlin.collections.set

class JobRunner(
        val strong: Strong,
        val config: TaskStorage.JobConfig,
        val path: String,
        val buildNumber: Int,
        val dir: File,
        val bashPath: File) : Closeable {
    val out = FreezedStack<Out>().asFiFoQueue()
    private val logOutput by strong.service<LogOutput>()
//    private val client by strong.service<ClientThread>()

    //    private var events: JobActionListener? = null
    private var cancelled = false
    private val log = Logger.getLog("Runner $path:$buildNumber")

//    private fun actionProcess() {
//        val events = events!!.actions
//        while (!events.isEmpty) {
//            val event = events.pop()
//            when (event) {
//                is ActionCancel -> cancelled = true
//            }
//        }
//    }

    private fun makeStatusDto(status: TaskStorage.JobStatusType) = Action.TaskStatusChange(
            path = path,
            buildNumber = buildNumber,
            status = status
    )

    var done: Boolean = false
        private set

    var status = TaskStorage.JobStatusType.PREPARE
        private set

    val eventStatusUpdate = EventElement()

    private val runnerThread = object : Thread() {
        override fun run() {
            build()
        }
    }

    fun start() {
        runnerThread.start()
    }

    fun build() {
        log.info("Start Building...")
//        events = JobActionListener(
//                url = URL(url),
//                job = job.toProcess()
//        )
        try {
            val scriptFile = File(dir, "script.sh")
            scriptFile.write().use {
                it.utf8Appendable().append(config.cmd)
            }
            val env = HashMap(Environment.getEnvs())
            env.putAll(config.env)
            env["BUILD_NUMBER"] = buildNumber.toString()
            env.putAll(env)
            println("Change status")
//            client.state = SlaveService.SlaveStatus(
//                    jobPath = path,
//                    buildNumber = buildNumber
//            )
            status = TaskStorage.JobStatusType.PROCESS
            eventStatusUpdate.dispatch()
            println("Executing...")
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

            println("Reading output")
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
                        status = TaskStorage.JobStatusType.CANCELED
                        eventStatusUpdate.dispatch()
//                        client.send(makeStatusDto(TaskStorage.JobStatusType.CANCELED))
                        break
                    }
                    if (stderrDone && stdoutDone) {
                        break
                    }
                    val o = out.popOrNull()
                    if (o == null) {
                        println("Sleep on ${Thread.currentThread.id}")
                        Thread.sleep(1000)
                        continue
                    }
                    if (o.value == null) {
                        when (o.type) {
                            OutType.STDOUT -> stdoutDone = true
                            OutType.STDERR -> stderrDone = true
                        }
                    } else {
                        when (o.type) {
                            OutType.STDOUT -> logOutput.stdout(o.value)
                            OutType.STDERR -> logOutput.errout(o.value)
                        }
                    }
                } catch (e: Throwable) {
                    println("JobRunner::build::loop error: $e")
                }
            }
            stdout.interrupt()
            stderr.interrupt()
            try {
                status = if (process.exitStatus == 0)
                    TaskStorage.JobStatusType.FINISHED_OK
                else
                    TaskStorage.JobStatusType.FINISHED_ERROR
                eventStatusUpdate.dispatch()
//                client.send(makeStatusDto(status))
            } catch (e: Process.ProcessStillActive) {
                status = TaskStorage.JobStatusType.FINISHED_ERROR
                eventStatusUpdate.dispatch()
//                client.send(makeStatusDto(TaskStorage.JobStatusType.FINISHED_ERROR))
            }
        } catch (e: Throwable) {
            println("Error build ${path}:${buildNumber}   $e")
            status = TaskStorage.JobStatusType.FINISHED_ERROR
            eventStatusUpdate.dispatch()
//            client.send(makeStatusDto(TaskStorage.JobStatusType.FINISHED_ERROR))
        }
//        client.state = null
        done = true
        println("All is done!")
//        events?.close()
    }

    override fun close() {
        cancelled = true
    }
}