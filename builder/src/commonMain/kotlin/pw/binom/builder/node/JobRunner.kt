package pw.binom.builder.node

import pw.binom.*
import pw.binom.builder.common.JobDescription
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.file.File
import pw.binom.io.file.FileOutputStream
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.job.Worker
import pw.binom.job.execute
import pw.binom.process.Process
import pw.binom.process.execute

class JobRunner(url: String, val dir: File, val bashPath: File, val job: JobDescription, val client: AsyncHttpClient, val envs: Map<String, String>) {
    val url = url.removeSuffix("/")
    val out = FreezedStack<Out>().asFiFoQueue()
    suspend fun build() {
        try {
            val scriptFile = File(dir, "script.sh")
            FileOutputStream(scriptFile).use {
                it.utf8Appendable().append(job.cmd)
            }
            val env = HashMap(Environment.getEnvs())
            env.putAll(this.envs)
            env["BUILD_NUMBER"] = job.buildNumber.toString()
            env.putAll(job.env)
            val process = Process.execute(
                    path = bashPath.path,
                    workDir = dir.path,
                    args = listOf(scriptFile.path),
                    env = env)
            val stdout = Worker.execute {
                ThreadReader(process.stdout, Out.Type.OUT, out)
            }

            val stderr = Worker.execute {
                ThreadReader(process.stderr, Out.Type.ERR, out)
            }

            var stdoutDone = false
            var stderrDone = false

            while (true) {
                try {
                    if (stderrDone && stdoutDone) {
                        break
                    }
                    val o = out.popOrNull()
                    if (o == null) {
                        Thread.sleep(1)
                        continue
                    }
                    if (o.value == null) {
                        when (o.type) {
                            Out.Type.OUT -> stdoutDone = true
                            Out.Type.ERR -> stderrDone = true
                        }
                    } else {
                        sendOut(o)
                    }
                } catch (e: Throwable) {
                    println("JobRunner::build::loop error: $e")
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
    }

    private suspend fun sendFinish(ok: Boolean) {
        client.request(method = "POST", url = URL("$url/execution/${job.path.removePrefix("/")}/${job.buildNumber}/finish")).use {
            val txt = if (ok) "true" else "false"
            it.addRequestHeader(Headers.CONTENT_LENGTH, txt.length.toString())
            it.outputStream.utf8Appendable().append(txt)
            it.outputStream.flush()
        }
    }

    private suspend fun sendOut(out: Out) {
        val e = when (out.type) {
            Out.Type.OUT -> "stdout"
            Out.Type.ERR -> "stderr"
        }
        client.request(method = "POST", url = URL("$url/execution/${job.path.removePrefix("/")}/${job.buildNumber}/$e")).use { req ->
            ByteArrayOutputStream().use { b ->
                b.utf8Appendable().append(out.value)
                b.flush()
                val data = b.toByteArray()
                req.addRequestHeader(Headers.CONTENT_LENGTH, data.size.toString())
                req.outputStream.write(data)
                req.outputStream.flush()
            }
        }
    }
}