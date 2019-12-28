package pw.binom.builder.web

import org.w3c.xhr.XMLHttpRequest
import pw.binom.builder.remote.AbstractClient
import pw.binom.builder.remote.DTO_LIST
import pw.binom.builder.remote.JobProcess
import pw.binom.builder.web.dto.LastOutDto
import pw.binom.io.Closeable
import pw.binom.io.asAsync
import pw.binom.json.*
import pw.binom.krpc.Struct
import pw.binom.krpc.StructFactory
import pw.binom.rpc.JsonRpc
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object API

object Client : AbstractClient() {
    override fun events(func: (Struct?) -> Unit): Closeable =
            Request.tail("/api/events") {
                if (it == null) {
                    func(null)
                    return@tail
                }
                async {
                    func(JsonRpc.fromJSON(DTO_LIST, it.parseJSON(), StructFactory.Class.Any(false)) as Struct)
                }
            }

    override fun tail(process: JobProcess, func: (String?) -> Unit): Closeable =
            Request.tail("/api/tail?path=${process.path.encodeUrl()}&build=${process.buildNumber}", func)

    override suspend fun call(service: String, args: JsonObject): JsonObject {
        return suspendCoroutine { cor ->
            val rr = XMLHttpRequest()
            rr.withCredentials = true

            rr.onreadystatechange = {
                async {
                    if (rr.readyState == XMLHttpRequest.DONE) {
                        val txt = rr.responseText
                        val json = txt.parseJSON()
                        console.info("original", txt)
                        console.info("parsed: ", json)
                        cor.resume(json.obj)
                    }
                }
            }
            async {
                rr.open(method = "POST", url = "$serverUrl/api/$service", async = true)
                val sb = StringBuilder()
                args.write(sb.asAsync())
                rr.send(sb.toString())
            }
        }
    }

}

//interface AsyncResponse<T> : Closeable {
//    fun subscribe(func: (T) -> Unit): Closeable
//}

//suspend fun API.getTasks(path: String): List<JobEntity> {
//    val result = Request.get("/tasks/${path.removePrefix("/")}").await()
//    val json = JSON.parse<Json>(result.body).unsafeCast<Array<Json>>()
//
//    return json.map {
//        JobEntity.read(it)
//    }
//}

//suspend fun API.getTask(path: String): JobInformation {
//    val result = Request.get("/tasks/${path.removePrefix("/")}").await()
//    val json = JSON.parse<Json>(result.body)
//    return JobInformation.read(json)
//}
/*
suspend fun API.executeTask(path: String): JobProcess {
    val result = Request.post("/tasks/${path.removePrefix("/")}/execute").await()
    val json = JSON.parse<Json>(result.body)
    return ExecuteJob.read(json)
}
*/

//fun API.tailTask(path: String, buildNumber: Long, listener: (String?) -> Unit) =
//        Request.tail("/execution/${path.removePrefix("/")}/$buildNumber/tail", listener)

//fun API.tailEvents(listener: (String?) -> Unit) =
//        Request.tail("/events/tail", listener)

//suspend fun API.createTask(path: String, name: String, job: JobInformation): JobEntity.Job {
//    val r = Request.post("/tasks/${path.removePrefix("/")}/createJob?name=${encodeURIComponent(name)}", job.write())
//            .await()
//    return JobEntity.read(JSON.parse(r.body)) as JobEntity.Job
//}
//
//suspend fun API.createDir(path: String, name: String): JobEntity.Folder {
//    val r = Request.post("/tasks/${path.removePrefix("/")}/createDir?name=${encodeURIComponent(name)}")
//            .await()
//    return JobEntity.read(JSON.parse(r.body)) as JobEntity.Folder
//}
/*
suspend fun API.getTaskBuilds(path: String): List<JobStatus> {
    val result = Request.get("/tasks/${path.removePrefix("/")}/builds").await()
    val json = JSON.parse<Array<Json>>(result.body)
    return json.map {
        JobStatus.read(it)
    }
}
*/

suspend fun API.getTaskOutputLast(path: String, buildNumber: Long, size: Int): LastOutDto {
    val result = Request.get("/tasks/${path.removePrefix("/")}/$buildNumber/outputlast?size=$size").await()
    return LastOutDto.read(JSON.parse(result.body))
}

suspend fun API.getTaskOutputFirst(path: String, buildNumber: Long, size: Long): String {
    val result = Request.get("/tasks/${path.removePrefix("/")}/$buildNumber/outputfirst?size=$size").await()
    return result.body
}


//suspend fun API.setTask(path: String, job: JobInformation) {
//    Request.post("/tasks/${path.removePrefix("/")}", job.write()).await()
//}

fun getNameFromPath(path: String) = path.split('/').last()