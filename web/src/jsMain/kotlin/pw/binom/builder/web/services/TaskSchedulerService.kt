package pw.binom.builder.web.services

import pw.binom.builder.web.Request1
import pw.binom.builder.web.encodeUrl

object TaskSchedulerService {
    suspend fun run(path: String): Int =
            Request1.post("/api/scheduler/run/${path.encodeUrl()}")!!.toInt()
}