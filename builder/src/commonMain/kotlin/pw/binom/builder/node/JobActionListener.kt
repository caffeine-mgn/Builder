package pw.binom.builder.node

import pw.binom.*
import pw.binom.builder.common.Action
import pw.binom.builder.common.ExecuteJob
import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.use
import pw.binom.io.utf8Reader
import pw.binom.job.Task
import pw.binom.job.Worker
import pw.binom.job.execute
import pw.binom.json.JsonDomReader
import pw.binom.json.JsonReader
import pw.binom.json.array

class JobActionListener(url: URL, job: ExecuteJob) : Closeable {
    override fun close() {
        job.interrupt()
    }

    val event = FreezedStack<Action>().asFiFoQueue()
    val actions: Queue<Action>
        get() = event

    private val job = Worker.execute {
        JobThread(url, job, event)
    }

    class JobThread(val url: URL, val job: ExecuteJob, val queue: AppendableQueue<Action>) : Task() {

        private val manager = ConnectionManager()
        private val client = AsyncHttpClient(manager)

        fun exe() {
            async {
                while (!isInterrupted) {
                    try {
                        client.request("GET", URL(url.toString().removeSuffix("/") + "/execution/${job.path}/${job.buildNumber}/actions")).use {
                            when (val code = it.responseCode()) {
                                200 -> {
                                    var count = 0
                                    val r = JsonDomReader()
                                    JsonReader(it.inputStream.utf8Reader()).accept(r)
                                    r.node.array.map {
                                        Action.read(it)
                                    }.forEach {
                                        queue.push(it)
                                        count++
                                    }
                                }
                                else-> println("Invalid response code: $code")
                            }
                        }
                    } catch (e: IOException) {
                        println("ERROR: $e")
                    }
                    Thread.sleep(1_000)
                }
            }
        }

        override fun execute() {
            exe()
            while (!isInterrupted) {
                manager.update(1000)
            }
            manager.close()
        }

    }
}