package pw.binom.builder.node
/*
import pw.binom.*
import pw.binom.builder.remote.JobProcess
import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.ConnectionManager
import pw.binom.job.Task
import pw.binom.job.Worker
import pw.binom.job.execute
import pw.binom.krpc.Struct
import pw.binom.thread.FreezedStack
import pw.binom.thread.Thread

class JobActionListener(url: URL, job: JobProcess) : Closeable {
    override fun close() {
        job.interrupt()
    }

    val event = FreezedStack<Struct>().asFiFoQueue()
    val actions: Queue<Struct>
        get() = event

    private val job = Worker.execute {
        JobThread(url, job, event)
    }

    class JobThread(val url: URL, val job: JobProcess, val queue: AppendableQueue<Struct>) : Task() {
        override fun execute() {
            val manager = ConnectionManager()
            val client = AsyncHttpClient(manager)
            async {
                while (!isInterrupted) {
                    try {
                        TODO()
                        /*
                        client.request("GET", URL(url.toString().removeSuffix("/") + "/execution/${job.path}/${job.buildNumber}/actions")).use {
                            when (val code = it.responseCode()) {
                                200 -> {
                                    var count = 0
                                    val r = JsonDomReader()
                                    JsonReader(it.inputStream.utf8Reader()).accept(r)
                                    r.node.array.map {
                                        Action.read(it!!)
                                    }.forEach {
                                        queue.push(it)
                                        count++
                                    }
                                }
                                else-> println("Invalid response code: $code")
                            }
                        }
                        */
                    } catch (e: IOException) {
                        println("ERROR: $e")
                    }
                    Thread.sleep(1_000)
                }
            }
            while (!isInterrupted) {
                manager.update(1000)
            }
            manager.close()
        }

    }
}
*/