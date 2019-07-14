package pw.binom.builder.server

import pw.binom.Platform
import pw.binom.Thread
import pw.binom.builder.common.ExecuteJob
import pw.binom.builder.common.JobDescription
import pw.binom.io.Closeable
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ExecuteControl : Closeable {

    override fun close() {
        waitList.forEach {
            it.continuation.resumeWithException(RuntimeException("Cancel"))
        }
    }

    private class WaitRecord(val continuation: Continuation<JobDescription?>, var timeout: Long, val platform: Platform)

    private val tasks = ArrayList<JobDescription>()
    private val waitList = ArrayList<WaitRecord>()
    private var lastTime = Thread.currentTimeMillis()

    fun update() {
        val dt = Thread.currentTimeMillis() - lastTime
        lastTime = Thread.currentTimeMillis()
        val it = waitList.iterator()
        while (it.hasNext()) {
            val e = it.next()
            e.timeout -= dt
            if (e.timeout <= 0) {
                e.continuation.resume(null)
                it.remove()
            }
        }
    }

    suspend fun getExecute(platform: Platform, timeout: Long): JobDescription? {
        val job = tasks.asSequence().filter { it.platform == null || it.platform == platform }.firstOrNull()
        if (job != null)
            return job
        return suspendCoroutine { v ->
            waitList += WaitRecord(v, timeout, platform)
        }
    }

    suspend fun execute(job: TaskManager.Job): ExecuteJob {
        val file = job.jobFile()
        val ej = JobDescription(buildNumber = file.nextBuild, path = job.path, cmd = file.cmd, env = file.env, platform = file.platform)
        job.start(ej.buildNumber)
        file.incNextBuild()
        val w = waitList.asSequence().filter { ej.platform == null || ej.platform == it.platform }.firstOrNull()
        return if (w != null) {
            waitList.remove(w)
            w.continuation.resume(ej)
            ej.toExecuteJob()
        } else {
            tasks.add(ej)
            ej.toExecuteJob()
        }
    }
}