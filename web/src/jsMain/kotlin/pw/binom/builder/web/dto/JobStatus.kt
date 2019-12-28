package pw.binom.builder.web.dto

import pw.binom.builder.web.asJson
import kotlin.js.Json
/*
enum class JobStatusType {
    PREPARE,
    PROCESS,
    FINISHED_OK,
    FINISHED_ERROR,
    CANCELED
}

class JobStatus(val job: ExecuteJob, val start: Long?, val end: Long?, val node: NodeInfo?, val status: JobStatusType?) {
    fun newStatus(status: JobStatusType) =
            JobStatus(
                    job = job,
                    start = start,
                    end = end,
                    node = node,
                    status = status
            )

    companion object {
        fun read(node: Json) =
                JobStatus(
                        job = ExecuteJob.read(node["job"]!!.asJson),
                        status = node["status"]?.toString()?.let { JobStatusType.valueOf(it) },
                        end = node["end"]?.toString()?.let { it.toLong() },
                        start = node["start"]?.toString()?.let { it.toLong() },
                        node = node["node"]?.asJson?.let { NodeInfo.read(it) }
                )
    }
}

 */