package pw.binom.builder.node

import pw.binom.URL
import pw.binom.builder.client.Client
import pw.binom.builder.remote.NodeDescription
import pw.binom.builder.remote.toProcess
import pw.binom.builder.sync
import pw.binom.io.IOException
import pw.binom.io.file.File
import pw.binom.io.file.iterator
import pw.binom.io.file.mkdirs
import pw.binom.io.file.name
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.use
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.thread.Thread

class Node(url: String, val id: String, val dataCenter: String, val bashPath: File, val buildPath: File, val envs: Map<String, String>) {
    private val LOG = Logger.getLog("Node")
    val url = url.removeSuffix("/")

    private val manager = ConnectionManager()
    private val httpClient = AsyncHttpClient(manager)

    val client = Client(URL(url), httpClient)

    fun start() {
        LOG.info("Start Build Node")
        val nodeInfo = NodeDescription(id = id, tags = emptyList())
        val passThread = PassThread(URL(url), nodeInfo)
        passThread.start()
        while (true) {
            try {

                LOG.info("Getting event...")
                val job = manager.sync { client.processService.popBuild(nodeInfo) }
                if (job == null) {
                    LOG.info("No job")
                    Thread.sleep(10_000)
                    continue
                }
                passThread.job = job.toProcess()
                LOG.info("Event got!")
                val dir = File(buildPath, (buildPath.lastBuild()!! + 1).toString())
                dir.mkdirs()
                LOG.info("Start job ${job.path}:${job.buildNumber}")
                manager.sync {
                    JobRunner(
                            dir = dir,
                            job = job,
                            bashPath = bashPath,
                            client = client,
                            url = url,
                            envs = envs,
                            passThread = passThread
                    ).build()
                }
                passThread.job = null
                LOG.info("Build Done")
            } catch (e: IOException) {
                Thread.sleep(10_000)
                continue
            }
        }
    }
}

/**
 * Searching last build dir. If [this] is not directory or [this] not exist then function returns null
 *
 * @return last build number
 */
fun File.lastBuild(): Long? {
    if (!isDirectory)
        return null
    var last: Long = 0
    iterator().use {
        it.forEach {
            if (!it.isDirectory)
                return@forEach
            val num = it.name.toLongOrNull() ?: return@forEach
            if (num > last)
                last = num
        }
    }
    return last
}