package pw.binom.builder.node

import pw.binom.*
import pw.binom.builder.common.JobDescription
import pw.binom.builder.common.NodeInfo
import pw.binom.io.*
import pw.binom.io.file.File
import pw.binom.io.file.iterator
import pw.binom.io.file.mkdirs
import pw.binom.io.file.name
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.ConnectionManager
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.logger.warn

class Node(url: String, val id: String, val dataCenter: String, val bashPath: File, val buildPath: File, val envs: Map<String, String>) {
    private val LOG = Logger.getLog("Node")
    val url = url.removeSuffix("/")
    fun start() {
        LOG.info("Start Build Node")
        val manager = ConnectionManager()
        val ff = AsyncHttpClient(manager)
        val nodeInfo = NodeInfo(id = id, platform = Environment.platform, dataCenter = dataCenter)
        async {
            while (true) {
                try {
                    LOG.info("Getting event")
                    val r = ff.request("POST", URL("$url/event"))
                    val sb = StringBuilder()
                    nodeInfo.write(sb.asAsync())
                    r.addRequestHeader("Content-Length", sb.length.toString())
                    r.outputStream.utf8Appendable().append(sb.toString())
                    r.outputStream.flush()

                    if (r.responseCode() != 200) {
                        LOG.info("No event. Exit code: ${r.responseCode()}")
                        Thread.sleep(1000)
                        continue
                    }
                    LOG.info("Event got!")
                    val job = JobDescription.read(r.inputStream.utf8Reader())
                    r.close()
                    val dir = File(buildPath, (buildPath.lastBuild()!! + 1).toString())
                    dir.mkdirs()
                    LOG.info("Start job ${job.path}:${job.buildNumber}")
                    JobRunner(
                            dir = dir,
                            job = job,
                            bashPath = bashPath,
                            client = ff,
                            url = url,
                            envs = envs
                    ).build()
                    LOG.info("Build Done")
                } catch (e: IOException) {
                    LOG.warn("ERROR: $e")
                    Thread.sleep(1000)
                    continue
                }
            }
        }

        while (true) {
            manager.update()
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