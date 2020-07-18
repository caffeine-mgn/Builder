package pw.binom.builder.client
/*
import pw.binom.URL
import pw.binom.async
import pw.binom.builder.common.JobEntity
import pw.binom.builder.encodeUrl
import pw.binom.builder.remote.AbstractClient
import pw.binom.builder.remote.JobProcess
import pw.binom.io.*
import pw.binom.io.httpClient.AsyncHttpClient
import pw.binom.io.socket.ConnectionManager
import pw.binom.json.*
import pw.binom.krpc.Struct

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

class Client(val serverUrl: URL,client:AsyncHttpClient) : AbstractClient() {
    override fun events(func: (Struct?) -> Unit): Closeable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun tail(process: JobProcess, func: (String?) -> Unit): Closeable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun call(service: String, args: JsonObject): JsonObject {
        val manager = ConnectionManager()
        val client = AsyncHttpClient(manager)
        var result: JsonObject? = null
        async {
            result = client.request("POST", url("api/$service")).use {
                args.write(it.outputStream.utf8Appendable())
                it.outputStream.flush()
                check(it.responseCode() == 200) { "Server responded ${it.responseCode()}" }
                it.inputStream.utf8Reader().parseJSON().obj
            }
        }

        while (result == null) {
            manager.update()
        }
        return result!!
    }


//    class InvalidServerReponseCodeException(code: Int) : RuntimeException("Invalid server response: $code")

    private fun url(path: String): URL {
        return URL("${serverUrl.toString().removeSuffix("/")}/${path.removePrefix("/")}")
    }
/*
    fun tail(job: String, buildNumber: Long, appendable: Appendable) {
        val manager = ConnectionManager()
        val client = AsyncHttpClient(manager)
        var done = false
        var exception: Throwable? = null
        async {
            client.request("GET", url("/execution/${job.encodeUrl()}/$buildNumber/tail")).use {
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
*/
    /*
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
                                JobStatus.read(it!!)
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
    */
//    suspend fun execute(job: String) = processService.execute(job)

//    suspend fun cancel(job: String, buildNumber: Long) = processService.cancel(JobProcess(buildNumber = buildNumber, path = job))
/*
    fun tasks(path: String): List<JobEntity> {
        val manager = ConnectionManager()
        val client = AsyncHttpClient(manager)
        var done = false
        var result: List<JobEntity>? = null
        var exception: Throwable? = null
        async {
            client.request("GET", url("/tasks/${path.removePrefix("/").encodeUrl()}")).use {
                val code = it.responseCode()
                when (val e = code) {
                    200 -> {
                        val r = JsonDomReader()
                        JsonReader(it.inputStream.utf8Reader()).accept(r)
                        result = r.node.array.map {
                            JobEntity.read(it!!)
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
*/
    /*
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
                            NodeStatus.read(it!!)
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
    */
}
*/