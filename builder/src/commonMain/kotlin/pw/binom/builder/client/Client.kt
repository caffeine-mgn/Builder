package pw.binom.builder.client

import pw.binom.URL
import pw.binom.async
import pw.binom.builder.common.ExecuteJob
import pw.binom.builder.common.JobEntity
import pw.binom.builder.common.JobStatus
import pw.binom.builder.common.NodeStatus
import pw.binom.io.*
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.ConnectionManager
import pw.binom.json.JsonDomReader
import pw.binom.json.JsonReader
import pw.binom.json.array

suspend fun AsyncReader.readln1(): String? {
    val sb = StringBuilder()
    try {
        while (true) {
            val r = read() ?: break
            if (r == 10.toChar())
                break
            if (r == 13.toChar()) {
                continue
            }
            sb.append(r)
        }
    } catch (e: EOFException) {
        //NOP
    }
    if (sb.isEmpty())
        return null
    return sb.toString()
}

class Client(val serverUrl: URL) {
    class InvalidServerReponseCodeException(code: Int) : RuntimeException("Invalid server response: $code")
    class JobNotFoundException : RuntimeException()

    private fun url(path: String) = URL("${serverUrl.toString().removeSuffix("/")}$path")

    fun tail(job: String, buildNumber: Long, appendable: Appendable) {
        val manager = ConnectionManager()
        val client = AsyncHttpClient(manager)
        var done = false
        var exception: Throwable? = null
        async {
            client.request("GET", url("/execution/$job/$buildNumber/tail")).use {
                when (val code = it.responseCode()) {
                    200, 204 -> {
                        val reader = it.inputStream.utf8Reader()
                        while (true) {
                            appendable.append(reader.readln() ?: break).append("\n")
                        }
                    }
                    else -> exception = InvalidServerReponseCodeException(code)
                }
            }
            done = true
        }

        while (!done) {
            manager.update(1000)
        }
        client.close()
        manager.close()
        if (exception != null)
            throw exception!!
    }

    fun executions(): List<JobStatus> {
        val manager = ConnectionManager()
        val client = AsyncHttpClient(manager)
        var done = false
        var result: List<JobStatus>? = null
        var exception: Throwable? = null
        async {
            client.request("GET", url("/execution/")).use {
                val code = it.responseCode()
                when (val e = code) {
                    200 -> {
                        val r = JsonDomReader()
                        JsonReader(it.inputStream.utf8Reader()).accept(r)
                        result = r.node.array.map {
                            JobStatus.read(it)
                        }
                    }
                    else -> exception = InvalidServerReponseCodeException(code)
                }
            }
            done = true
        }

        while (!done) {
            manager.update(1000)
        }
        client.close()
        manager.close()
        if (exception != null)
            throw exception!!
        return result!!
    }

    fun execute(job: String): ExecuteJob {
        val manager = ConnectionManager()
        val client = AsyncHttpClient(manager)
        var done = false
        var executeJob: ExecuteJob? = null
        var exception: Throwable? = null
        async {
            client.request("POST", url("/tasks/$job/execute")).use {
                val code = it.responseCode()
                when (val e = code) {
                    200, 204 -> {
                        executeJob = ExecuteJob.read(it.inputStream.utf8Reader())
                    }
                    else -> exception = InvalidServerReponseCodeException(code)
                }
            }
            done = true
        }

        while (!done) {
            manager.update(1000)
        }
        client.close()
        manager.close()
        if (exception != null)
            throw exception!!
        return executeJob!!
    }

    fun cancel(job: String, buildNumber: Long) {
        val manager = ConnectionManager()
        val client = AsyncHttpClient(manager)
        var done = false
        var exception: Throwable? = null
        async {
            client.request("POST", url("/execution/$job/$buildNumber/cancel")).use {
                when (val code = it.responseCode()) {
                    200, 204 -> {
                    }
                    else -> exception = InvalidServerReponseCodeException(code)
                }
            }
            done = true
        }

        while (!done) {
            manager.update(1000)
        }
        client.close()
        manager.close()
        if (exception != null)
            throw exception!!
    }

    fun tasks(path: String): List<JobEntity> {
        val manager = ConnectionManager()
        val client = AsyncHttpClient(manager)
        var done = false
        var result: List<JobEntity>? = null
        var exception: Throwable? = null
        async {
            client.request("GET", url("/tasks/${path.removePrefix("/")}")).use {
                val code = it.responseCode()
                when (val e = code) {
                    200 -> {
                        val r = JsonDomReader()
                        JsonReader(it.inputStream.utf8Reader()).accept(r)
                        result = r.node.array.map {
                            JobEntity.read(it)
                        }
                    }
                    else -> exception = InvalidServerReponseCodeException(code)
                }
            }
            done = true
        }

        while (!done) {
            manager.update(1000)
        }
        client.close()
        manager.close()
        if (exception != null)
            throw exception!!
        return result!!
    }

    fun status(): List<NodeStatus> {
        val manager = ConnectionManager()
        val client = AsyncHttpClient(manager)
        var done = false
        var result: List<NodeStatus>? = null
        var exception: Throwable? = null
        async {
            client.request("GET", URL("${serverUrl.toString().removeSuffix("/")}/nodes/status")).use {
                val code = it.responseCode()
                when (val e = code) {
                    200 -> {
                        val r = JsonDomReader()
                        JsonReader(it.inputStream.utf8Reader()).accept(r)
                        result = r.node.array.map {
                            NodeStatus.read(it)
                        }
                    }
                    else -> exception = InvalidServerReponseCodeException(code)
                }
            }
            done = true
        }

        while (!done) {
            manager.update(1000)
        }
        client.close()
        manager.close()
        if (exception != null)
            throw exception!!
        return result!!
    }
}