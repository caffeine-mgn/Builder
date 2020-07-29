package pw.binom.builder.node
/*
import pw.binom.URL
import pw.binom.async
import pw.binom.builder.common.Job
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.readln
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.utf8Reader
import pw.binom.thread.Thread

class JobListener(val slave: Client) : Thread() {
    val manager = SocketNIOManager()
    val client = AsyncHttpClient(manager)

    override fun run() {
        val serverUrl = slave.serverUrl.newURI("${slave.serverUrl.uri.removeSuffix("/")}/job")
        val request = client.request("GET", serverUrl).addHeader("X-Slave-Id", slave.slaveId.toString())
        async {
            val response = request.response().utf8Reader()
            while (!isInterrupted) {
                val jobJson = response.readln()
                if (jobJson == null) {
                    interrupt()
                    break
                }
                val job = Job.toJob(jobJson)
                job.execute(slave)
            }
        }
        while (!isInterrupted) {
            manager.update(1000)
        }
        client.close()
        manager.close()
    }
}
*/