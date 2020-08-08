package pw.binom.builder.web.services

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import pw.binom.builder.dto.Worker
import pw.binom.builder.web.Request1
import pw.binom.builder.web.UnauthorizedException

object WorkerService {
    suspend fun getList() =
            try {
                Json.parse(Worker.serializer().list, Request1.get("/api/workers"))
            } catch (e: UnauthorizedException) {
                null
            }
}