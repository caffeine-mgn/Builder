package pw.binom.builder.web.dto
/*
import pw.binom.builder.remote.JobProcess
import pw.binom.builder.remote.JobStatusType
import pw.binom.builder.web.asJson
import kotlin.js.Json

sealed class Event {
    class JobChangeState(val executeJob: JobProcess, val status: JobStatusType) : Event()
    class JobEntityCreated(val entity: JobEntity) : Event()
    class JobEntityDeleted(val entity: JobEntity) : Event()
    class JobEntityModify(val entity: JobEntity) : Event()

    companion object {
        suspend fun read(node: Json): Event {
            val type = node["type"]!!.toString()
            return when (type) {
                //"JobChangeState" -> JobChangeState(ExecuteJob.read(node["job"]!!.asJson), node["status"]!!.toString().let { JobStatusType.valueOf(it) })
                "JobEntityCreated" -> JobEntityCreated(JobEntity.read(node["job"]!!.asJson))
                "JobEntityDeleted" -> JobEntityDeleted(JobEntity.read(node["job"]!!.asJson))
                "JobEntityModify" -> JobEntityModify(JobEntity.read(node["job"]!!.asJson))
                else -> TODO()
            }
        }
    }
}
*/