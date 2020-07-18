package pw.binom.builder.master

sealed class Out(val message: String) {
    class Std(message: String) : Out(message)
    class Err(message: String) : Out(message)
}
/*
class ProcessServiceImpl(val taskManager: TaskManager1, val eventTopic: Topic<Struct>) : ProcessServiceAsync {
    override suspend fun cancelled(task: JobProcess) {
    }

    private fun String.toProcess(): JobProcess {
        val items = split(":")
        return JobProcess(
                path = items[0],
                buildNumber = items[1].toLong()
        )
    }

    override suspend fun stdout(build: String, message: String): Boolean {
        val process = getProcess(build.toProcess())
        if (process.status != JobStatusType.PROCESS) {
            LOG.warn("Can't write new stdout: status of task us ${process.status}")
            return false
        }
        LOG.info("send stdout to process..")
        process.stdout(message)
        return true
    }

    override suspend fun stderr(build: String, message: String): Boolean {
        val process = getProcess(build.toProcess())
        if (process.status != JobStatusType.PROCESS) {
            return false
        }
        process.stderr(message)
        return true
    }

//    enum class ProcessResult {
//        CANCELED, FINISHED, ERROR, PROCESSING
//    }

    private var idIt = 0

    inner class Process(val build: JobProcess) {
        val id = ++idIt
        val topicOut = Topic<Out>()
        private val job = taskManager.getJob(build.path)

        private var _status = JobStatusType.PREPARE
        val status
            get() = _status

        init {
            val job = taskManager.getJob(build.path)

            if (job == null)
                LOG.warn("Task ${build.path}:${build.buildNumber} not found")
            else
                _status = job.getStatus(build.buildNumber).type
        }

        fun stdout(message: String) {
            topicOut.dispatch(Out.Std(message))
            job?.writeStdout(buildNum = build.buildNumber, txt = message)
            LOG.info("stdout($message). job=${job != null}, id=#$id")
        }

        fun stderr(message: String) {
            topicOut.dispatch(Out.Err(message))
            job?.writeStderr(buildNum = build.buildNumber, txt = message)
        }

        private fun dispatchNewStatus() {
            LOG.info("dispatch status: ${build.path}:${build.buildNumber} -> ${status.name}")
            eventTopic.dispatch(Event_TaskStatusChange(build, _status.name))
        }

        fun cancel() {
            _status = JobStatusType.CANCELED
            dispatchNewStatus()
            topicOut.close()
            job?.cancel(build.buildNumber)
        }

        fun finish(ok: Boolean) {
            _status = if (ok) JobStatusType.FINISHED_OK else JobStatusType.FINISHED_ERROR
            dispatchNewStatus()
            topicOut.close()
            job?.finish(build.buildNumber, ok)
        }

        fun start() {
            _status = JobStatusType.PROCESS
            dispatchNewStatus()
            job?.start(build.buildNumber)
        }
    }

    private val processes = ArrayList<Process>()

    fun getProcess(process: JobProcess): Process {
        var p = processes.find { it.build.buildNumber == process.buildNumber && it.build.path == process.path }
        if (p == null) {
            p = Process(process)
            processes += p
        }
        return p
    }

    private val LOG = Logger.getLog("Scheduler")

    override suspend fun popBuild(node: NodeDescription): BuildDescription? {
        val job = tasks.asSequence()
                .filter { isCanExecute(node, it.include, it.exclude) }
                .firstOrNull()
        if (job != null) {
            LOG.info("Found task for node ${node.id}")
            tasks.remove(job)
            return job
        }
        LOG.info("Can't found task for node ${node.id}. Task count: ${tasks.size}")
        return suspendCoroutine { v ->
            waitList += WaitRecord(v, 60_000, node)
        }
    }

    private class WaitRecord(val continuation: Continuation<BuildDescription?>, var timeout: Long, val node: NodeDescription)

    private fun isCanExecute(node: NodeDescription, include: List<String>, exclude: List<String>): Boolean {
        if (include.isNotEmpty()) {
            if (!include.any { i -> node.tags.any { it == i } })
                return false
        }

        if (exclude.isNotEmpty()) {
            if (include.any { i -> node.tags.any { it == i } })
                return false
        }
        return true
    }

    private val waitList = ArrayList<WaitRecord>()
    private val tasks = ArrayList<BuildDescription>()

    override suspend fun execute(path: String): JobProcess {
        val job = taskManager.getJob(path) ?: TODO("Job \"$path\" not found")
        val file = job.jobFile()
        val ej = BuildDescription(
                buildNumber = file.nextBuild,
                path = job.path,
                cmd = file.cmd,
                env = file.env.map { EnvVar(name = it.key, value = it.value) },
                include = file.include,
                exclude = file.exclude
        )
        job.start(ej.buildNumber)
        file.incNextBuild()
        val w = waitList
                .asSequence()
                .filter { isCanExecute(it.node, file.include, file.exclude) }
                .firstOrNull()
        return if (w != null) {
            waitList.remove(w)
            w.continuation.resume(ej)
            ej.toProcess()
        } else {
            tasks.add(ej)
            ej.toProcess()
        }
    }

    override suspend fun cancel(task: JobProcess) {
        getProcess(task).cancel()
        taskManager.getJob(task.path)?.cancel(task.buildNumber)
    }

    override suspend fun finish(task: JobProcess, ok: Boolean) {
        processes.find { it.build.buildNumber == task.buildNumber && it.build.path == task.path }?.finish(ok)
        val index = tasks.indexOfFirst {
            it.buildNumber == task.buildNumber && it.path == task.path
        }
        if (index >= 0)
            tasks.removeAt(index)
    }

    override suspend fun start(task: JobProcess) {
        getProcess(task).start()
    }
}
*/