package pw.binom.builder.node

import pw.binom.URL
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicReference
import pw.binom.builder.client.Client
import pw.binom.builder.remote.JobProcess
import pw.binom.builder.remote.NodeDescription
import pw.binom.builder.sync
import pw.binom.io.IOException
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.ConnectionManager
import pw.binom.thread.Thread

val NODE_TTL = 10_000


class PassThread(private val serverUrl: URL, val desc: NodeDescription) : Thread() {
    private var _job = AtomicReference<JobProcess?>(null)

    var job: JobProcess?
        get() = _job.value
        set(value) {
            _canceled.value = false
            println("Thread Pass: chane _canceled.value=${_canceled.value}")
            _job.value = value
        }

    private val manager = ConnectionManager()
    private val httpClient = AsyncHttpClient(manager)

    private val client = Client(serverUrl, httpClient)
    private val _canceled = AtomicBoolean(false)
    val canceled
        get() = _canceled.value

    override fun run() {
        println("Start Node Pass thread... serverUrl=$serverUrl")
        try {
            var lastJob: JobProcess? = null
            var lastSend = 0L
            while (!isInterrupted) {
                val needUpdate = lastJob != job || Thread.currentTimeMillis() - lastSend > NODE_TTL
                println("Try to send... needUpdate=$needUpdate")
                if (needUpdate) {
                    try {
                        println("Call server...")
                        manager.sync {
                            println("-->#1")
                            val v = client.nodesService.pass(desc, job)
                            _canceled.value = !(v == null || v)

                            println("Thread Pass: _canceled.value=${_canceled.value}")
                        }
                    } catch (e: IOException) {
                        //NOP
                        println("error: $e")
                    }
                    lastJob = job
                    lastSend = Thread.currentTimeMillis()
                }
                Thread.sleep(1_000)
            }
            httpClient.close()
            manager.close()
        } finally {
            println("Stop Node Pass thread...")
        }
    }
}